package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CancelInstanceAction(
        val workflowInstance: String?
) : Action {

    override fun execute(runner: TestRunner, testContext: TestContext) {
        val context = testContext.getContext(workflowInstance)

        runner.cancelWorkflowInstance(context)
    }
}