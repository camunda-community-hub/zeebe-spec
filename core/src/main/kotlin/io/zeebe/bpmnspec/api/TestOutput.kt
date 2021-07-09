package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.api.runner.ElementInstance
import io.zeebe.bpmnspec.api.runner.Incident
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceVariable

data class TestOutput(
        val context: ProcessInstanceContext,
        val state: WorkflowInstanceState,
        val elementInstances: List<ElementInstance>,
        val variables: List<WorkflowInstanceVariable>,
        val incidents: List<Incident>
)