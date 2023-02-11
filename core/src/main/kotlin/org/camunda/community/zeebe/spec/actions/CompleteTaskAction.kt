package org.camunda.community.zeebe.spec.actions

import org.camunda.community.zeebe.spec.api.Action
import org.camunda.community.zeebe.spec.api.TestContext
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

class CompleteTaskAction(
    val jobType: String,
    val variables: String
) : Action {

    override fun execute(
        actionExecutor: SpecActionExecutor,
        stateProvider: SpecStateProvider,
        testContext: TestContext
    ) {

        actionExecutor.completeTask(jobType, variables)
    }
}