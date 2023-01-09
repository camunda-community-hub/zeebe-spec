package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.instance.FlowElement
import io.camunda.zeebe.protocol.record.intent.IncidentIntent
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent
import io.camunda.zeebe.protocol.record.value.BpmnElementType
import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.dto.*
import io.zeebe.bpmnspec.runner.SpecStateProvider
import org.camunda.community.eze.RecordStream.events
import org.camunda.community.eze.RecordStream.withElementType
import org.camunda.community.eze.RecordStream.withKey
import org.camunda.community.eze.RecordStream.withProcessInstanceKey
import org.camunda.community.eze.ZeebeEngine
import java.io.ByteArrayInputStream

class EzeSpecStateProvider(
    zeebeEngineProvider: () -> ZeebeEngine
) : SpecStateProvider {

    private val zeebeEngine = zeebeEngineProvider()

    override fun getProcessInstanceKeys(): List<ProcessInstanceKey> {
        return zeebeEngine
            .processInstanceRecords()
            .withElementType(elementType = BpmnElementType.PROCESS)
            .map { it.value.processInstanceKey }
            .distinct()
    }

    override fun getProcessInstanceState(processInstanceKey: ProcessInstanceKey): ProcessInstanceState {
        val state = zeebeEngine
            .processInstanceRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .withElementType(elementType = BpmnElementType.PROCESS)
            .map { it.intent }
            .lastOrNull()

        return when (state) {
            ProcessInstanceIntent.ELEMENT_ACTIVATED -> ProcessInstanceState.ACTIVATED
            ProcessInstanceIntent.ELEMENT_COMPLETED -> ProcessInstanceState.COMPLETED
            ProcessInstanceIntent.ELEMENT_TERMINATED -> ProcessInstanceState.TERMINATED
            else -> ProcessInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(processInstanceKey: ProcessInstanceKey): List<ElementInstance> {
        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceKey)

        return zeebeEngine
            .processInstanceRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastElementInstanceRecord = records.last()
                val elementId = lastElementInstanceRecord.value.elementId

                ElementInstance(
                    elementId = elementId,
                    elementName = elementNameLookup(elementId),
                    state = when (lastElementInstanceRecord.intent) {
                        ProcessInstanceIntent.ELEMENT_ACTIVATED -> ElementInstanceState.ACTIVATED
                        ProcessInstanceIntent.ELEMENT_COMPLETED -> ElementInstanceState.COMPLETED
                        ProcessInstanceIntent.ELEMENT_TERMINATED -> ElementInstanceState.TERMINATED
                        ProcessInstanceIntent.SEQUENCE_FLOW_TAKEN -> ElementInstanceState.TAKEN
                        else -> ElementInstanceState.UNKNOWN
                    }
                )
            }
    }

    override fun getProcessInstanceVariables(processInstanceKey: ProcessInstanceKey): List<ProcessInstanceVariable> {
        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceKey)

        return zeebeEngine
            .variableRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastVariableRecordValue = records.last().value

                val scopeKey = lastVariableRecordValue.scopeKey
                val scopeElementId = getElementId(elementInstanceKey = scopeKey)

                ProcessInstanceVariable(
                    variableName = lastVariableRecordValue.name,
                    variableValue = lastVariableRecordValue.value,
                    scopeElementId = scopeElementId ?: "",
                    scopeElementName = scopeElementId?.let(elementNameLookup)
                )
            }
    }

    override fun getIncidents(processInstanceKey: ProcessInstanceKey): List<Incident> {
        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceKey)

        return zeebeEngine
            .incidentRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastIncidentRecord = records.last()

                val incidentElementId =
                    getElementId(elementInstanceKey = lastIncidentRecord.value.elementInstanceKey)

                Incident(
                    errorType = lastIncidentRecord.value.errorType.name,
                    errorMessage = lastIncidentRecord.value.errorMessage,
                    state = when (lastIncidentRecord.intent) {
                        IncidentIntent.CREATED -> IncidentState.CREATED
                        IncidentIntent.RESOLVED -> IncidentState.RESOLVED
                        else -> IncidentState.UNKNOWN
                    },
                    elementId = incidentElementId ?: "?",
                    elementName = incidentElementId?.let(elementNameLookup)
                )
            }
    }

    private fun getElementNameLookup(processInstanceKey: ProcessInstanceKey): (String) -> String? {
        val processDefinitionKey = zeebeEngine
            .processInstanceRecords()
            .events()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .firstOrNull()
            ?.value
            ?.processDefinitionKey
            ?: -1

        val processRecord = zeebeEngine
            .processRecords()
            .withKey(key = processDefinitionKey)
            .firstOrNull()
            ?.value

        return processRecord
            ?.let {
                val bpmnXml = it.resource
                val bpmnModelInstance = Bpmn.readModelFromStream(ByteArrayInputStream(bpmnXml))

                return { elementId ->
                    if (elementId == it.bpmnProcessId) {
                        it.bpmnProcessId
                    } else {
                        bpmnModelInstance.getModelElementById<FlowElement>(elementId)
                            ?.name
                    }
                }
            }
            ?: { _ -> null }
    }

    private fun getElementId(elementInstanceKey: ProcessInstanceKey): String? {
        return zeebeEngine
            .processInstanceRecords()
            .withKey(key = elementInstanceKey)
            .firstOrNull()
            ?.value
            ?.elementId
    }

    override fun close() {
        // nothing to do
    }
}