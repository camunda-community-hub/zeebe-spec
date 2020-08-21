package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CreateInstanceAction(
        val bpmnProcessId: String,
        val variables: String,
        val workflowInstanceAlias: String?
) : Action {

    override fun execute(runner: TestRunner, contexts: MutableMap<String, WorkflowInstanceContext>) {
        val context = runner.createWorkflowInstance(bpmnProcessId, variables)

        val alias = workflowInstanceAlias ?: bpmnProcessId
        contexts.put(alias, context)
    }
}