package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.dto.ElementInstance
import io.zeebe.bpmnspec.api.dto.Incident
import io.zeebe.bpmnspec.api.dto.ProcessInstanceState
import io.zeebe.bpmnspec.api.dto.ProcessInstanceVariable

data class TestOutput(
    val processInstanceKey: ProcessInstanceKey,
    val state: ProcessInstanceState,
    val elementInstances: List<ElementInstance>,
    val variables: List<ProcessInstanceVariable>,
    val incidents: List<Incident>
)