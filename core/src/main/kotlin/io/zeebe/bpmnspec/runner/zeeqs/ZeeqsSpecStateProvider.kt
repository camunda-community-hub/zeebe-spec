package io.zeebe.bpmnspec.runner.zeeqs

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.dto.*
import io.zeebe.bpmnspec.runner.SpecStateProvider

class ZeeqsSpecStateProvider(
    zeeqsClientProvider: () -> ZeeqsClient
) : SpecStateProvider {

    private val zeeqsClient: ZeeqsClient = zeeqsClientProvider()

    override fun getProcessInstanceKeys(): List<ProcessInstanceKey> {
        return zeeqsClient.getProcessInstanceKeys()
    }

    override fun getProcessInstanceState(processInstanceKey: ProcessInstanceKey): ProcessInstanceState {
        val state = zeeqsClient.getProcessInstanceState(processInstanceKey)
        return when (state) {
            "COMPLETED" -> ProcessInstanceState.COMPLETED
            "TERMINATED" -> ProcessInstanceState.TERMINATED
            "ACTIVATED" -> ProcessInstanceState.ACTIVATED
            else -> ProcessInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(processInstanceKey: ProcessInstanceKey): List<ElementInstance> {
        return zeeqsClient.getElementInstances(processInstanceKey = processInstanceKey)
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
    }

    override fun getProcessInstanceVariables(processInstanceKey: ProcessInstanceKey): List<ProcessInstanceVariable> {
        return zeeqsClient.getProcessInstanceVariables(processInstanceKey = processInstanceKey)
            .map {
                ProcessInstanceVariable(
                    variableName = it.name,
                    variableValue = it.value,
                    scopeElementId = it.scope?.elementId ?: "",
                    scopeElementName = it.scope?.elementName ?: ""
                )
            }
    }

    override fun getIncidents(processInstanceKey: ProcessInstanceKey): List<Incident> {
        return zeeqsClient.getIncidents(processInstanceKey = processInstanceKey)
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

    override fun close() {
    }
}