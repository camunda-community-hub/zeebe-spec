package org.camunda.community.zeebe.spec.actions

import org.camunda.community.zeebe.spec.api.Action
import org.camunda.community.zeebe.spec.api.TestContext
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

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