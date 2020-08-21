package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CreateInstanceAction(
        val bpmnProcessId: String,
        val variables: String,
        val workflowInstanceAlias: String?
) : Action {

    override fun execute(runner: TestRunner,testContext: TestContext) {
        val wfContext = runner.createWorkflowInstance(bpmnProcessId, variables)

        val alias = workflowInstanceAlias ?: bpmnProcessId
        testContext.storeContext(alias, wfContext)
    }
}