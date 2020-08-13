package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestRunner
import io.zeebe.bpmnspec.api.WorkflowInstanceContext

class CreateInstanceAction(
        val bpmnProcessId: String,
        val variables: String
) : Action {

    override fun execute(runner: TestRunner): WorkflowInstanceContext? {
        val context = runner.createWorkflowInstance(bpmnProcessId, variables)

        return context
    }
}