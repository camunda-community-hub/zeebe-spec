package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider

interface Action : Instruction {

    fun execute(
        actionExecutor: SpecActionExecutor,
        stateProvider: SpecStateProvider,
        testContext: TestContext
    )

}