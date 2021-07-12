package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CreateInstanceAction(
    val bpmnProcessId: String,
    val variables: String,
    val processInstanceAlias: String?
) : Action {

    override fun execute(runner: TestRunner, testContext: TestContext) {
        val wfContext = runner.createProcessInstance(bpmnProcessId, variables)

        val alias = processInstanceAlias ?: bpmnProcessId
        testContext.storeContext(alias, wfContext)
    }
}