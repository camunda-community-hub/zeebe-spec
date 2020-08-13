package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CreateInstanceAction(
        val bpmnProcessId: String,
        val variables: String,
        val workflowInstanceAlias: String?
) : Action {

    override fun execute(runner: TestRunner): Pair<String, WorkflowInstanceContext>? {
        val context = runner.createWorkflowInstance(bpmnProcessId, variables)

        return Pair(
                workflowInstanceAlias ?: bpmnProcessId,
                context
        )
    }
}