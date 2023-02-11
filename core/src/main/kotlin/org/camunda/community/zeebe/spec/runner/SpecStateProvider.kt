package org.camunda.community.zeebe.spec.runner

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.dto.ElementInstance
import org.camunda.community.zeebe.spec.api.dto.Incident
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceState
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceVariable

interface SpecStateProvider : AutoCloseable {

    fun getProcessInstanceKeys(): List<ProcessInstanceKey>

    fun getProcessInstanceState(processInstanceKey: ProcessInstanceKey): ProcessInstanceState

    fun getElementInstances(processInstanceKey: ProcessInstanceKey): List<ElementInstance>

    fun getProcessInstanceVariables(processInstanceKey: ProcessInstanceKey): List<ProcessInstanceVariable>

    fun getIncidents(processInstanceKey: ProcessInstanceKey): List<Incident>

}