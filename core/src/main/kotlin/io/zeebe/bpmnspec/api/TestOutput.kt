package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.api.runner.ElementInstance
import io.zeebe.bpmnspec.api.runner.Incident
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceVariable

data class TestOutput(
        val context: ProcessInstanceContext,
        val state: ProcessInstanceState,
        val elementInstances: List<ElementInstance>,
        val variables: List<ProcessInstanceVariable>,
        val incidents: List<Incident>
)