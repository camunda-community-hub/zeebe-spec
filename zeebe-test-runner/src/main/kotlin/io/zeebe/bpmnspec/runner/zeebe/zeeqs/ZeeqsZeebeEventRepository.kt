package io.zeebe.bpmnspec.runner.zeebe.zeeqs

import io.zeebe.bpmnspec.api.runner.ElementInstance
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.Incident
import io.zeebe.bpmnspec.api.runner.IncidentState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceVariable
import io.zeebe.bpmnspec.runner.zeebe.ZeebeEventRepository

class ZeeqsZeebeEventRepository(
    private val zeeqsClient: ZeeqsClient
) : ZeebeEventRepository {

    override fun getProcessInstanceKeys() = zeeqsClient.getProcessInstanceKeys()

    override fun getProcessInstanceState(
        processInstanceKey: Long
    ) = when (zeeqsClient.getProcessInstanceState(processInstanceKey)) {
        "COMPLETED" -> ProcessInstanceState.COMPLETED
        "TERMINATED" -> ProcessInstanceState.TERMINATED
        "ACTIVATED" -> ProcessInstanceState.ACTIVATED
        else -> ProcessInstanceState.UNKNOWN
    }

    override fun getElementInstances(
        processInstanceKey: Long
    ) = zeeqsClient.getElementInstances(processInstanceKey = processInstanceKey)
        .map {
            ElementInstance(
                elementId = it.elementId,
                elementName = it.elementName,
                state = when (it.state) {
                    "ACTIVATED" -> ElementInstanceState.ACTIVATED
                    "COMPLETED" -> ElementInstanceState.COMPLETED
                    "TERMINATED" -> ElementInstanceState.TERMINATED
                    "TAKEN" -> ElementInstanceState.TAKEN
                    else -> ElementInstanceState.UNKNOWN
                }
            )
        }

    override fun getProcessInstanceVariables(
        processInstanceKey: Long
    ) = zeeqsClient.getProcessInstanceVariables(processInstanceKey = processInstanceKey)
        .map {
            ProcessInstanceVariable(
                variableName = it.name,
                variableValue = it.value,
                scopeElementId = it.scope?.elementId ?: "",
                scopeElementName = it.scope?.elementName ?: ""
            )
        }

    override fun getIncidents(
        processInstanceKey: Long
    ) = zeeqsClient.getIncidents(processInstanceKey = processInstanceKey)
        .map {
            Incident(
                errorType = it.errorType,
                errorMessage = it.errorMessage,
                state = when (it.state) {
                    "CREATED" -> IncidentState.CREATED
                    "RESOLVED" -> IncidentState.RESOLVED
                    else -> IncidentState.UNKNOWN
                },
                elementId = it.elementInstance?.elementId ?: "",
                elementName = it.elementInstance?.elementName ?: ""
            )
        }
}