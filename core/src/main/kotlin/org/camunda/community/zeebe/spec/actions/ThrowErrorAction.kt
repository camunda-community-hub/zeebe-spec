package org.camunda.community.zeebe.spec.actions

import org.camunda.community.zeebe.spec.api.Action
import org.camunda.community.zeebe.spec.api.TestContext
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

class ThrowErrorAction(
    val jobType: String,
    val errorCode: String,
    val errorMessage: String
) : Action {

    override fun execute(
        actionExecutor: SpecActionExecutor,
        stateProvider: SpecStateProvider,
        testContext: TestContext
    ) {

        actionExecutor.throwError(
            jobType = jobType,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
    }
}