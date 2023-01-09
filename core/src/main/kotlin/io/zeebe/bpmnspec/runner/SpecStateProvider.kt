package io.zeebe.bpmnspec.runner

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.dto.ElementInstance
import io.zeebe.bpmnspec.api.dto.Incident
import io.zeebe.bpmnspec.api.dto.ProcessInstanceState
import io.zeebe.bpmnspec.api.dto.ProcessInstanceVariable

interface SpecStateProvider : AutoCloseable {

    fun getProcessInstanceKeys(): List<ProcessInstanceKey>

    fun getProcessInstanceState(processInstanceKey: ProcessInstanceKey): ProcessInstanceState

    fun getElementInstances(processInstanceKey: ProcessInstanceKey): List<ElementInstance>

    fun getProcessInstanceVariables(processInstanceKey: ProcessInstanceKey): List<ProcessInstanceVariable>

    fun getIncidents(processInstanceKey: ProcessInstanceKey): List<Incident>

}