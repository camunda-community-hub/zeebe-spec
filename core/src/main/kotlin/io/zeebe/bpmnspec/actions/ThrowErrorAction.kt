package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider

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