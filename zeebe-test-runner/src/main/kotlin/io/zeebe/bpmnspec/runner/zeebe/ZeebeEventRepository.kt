package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.runner.ElementInstance
import io.zeebe.bpmnspec.api.runner.Incident
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceVariable

interface ZeebeEventRepository {

    fun getProcessInstanceKeys(): List<Long>

    fun getProcessInstanceState(processInstanceKey: Long): ProcessInstanceState

    fun getElementInstances(processInstanceKey: Long): List<ElementInstance>

    fun getProcessInstanceVariables(processInstanceKey: Long): List<ProcessInstanceVariable>

    fun getIncidents(processInstanceKey: Long): List<Incident>
}