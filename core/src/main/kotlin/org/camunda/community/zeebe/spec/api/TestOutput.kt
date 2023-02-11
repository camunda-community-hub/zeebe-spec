package org.camunda.community.zeebe.spec.api

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.dto.ElementInstance
import org.camunda.community.zeebe.spec.api.dto.Incident
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceState
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceVariable

data class TestOutput(
    val processInstanceKey: ProcessInstanceKey,
    val state: ProcessInstanceState,
    val elementInstances: List<ElementInstance>,
    val variables: List<ProcessInstanceVariable>,
    val incidents: List<Incident>
)