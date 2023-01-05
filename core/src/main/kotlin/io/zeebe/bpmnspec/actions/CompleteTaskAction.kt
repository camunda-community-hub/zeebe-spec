package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider

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