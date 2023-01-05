package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider

class CreateInstanceAction(
    val bpmnProcessId: String,
    val variables: String,
    val processInstanceAlias: String?
) : Action {

    override fun execute(
        actionExecutor: SpecActionExecutor,
        stateProvider: SpecStateProvider,
        testContext: TestContext
    ) {

        val wfContext = actionExecutor.createProcessInstance(bpmnProcessId, variables)

        val alias = processInstanceAlias ?: bpmnProcessId
        testContext.storeContext(alias, wfContext)
    }
}