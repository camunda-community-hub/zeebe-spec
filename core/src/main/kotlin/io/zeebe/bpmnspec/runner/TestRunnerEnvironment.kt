package io.zeebe.bpmnspec.runner

import io.zeebe.bpmnspec.api.SpecTestRunnerContext

interface TestRunnerEnvironment : AutoCloseable {

    val actionExecutor: SpecActionExecutor
    val stateProvider: SpecStateProvider

    fun create()

    fun getContext(): SpecTestRunnerContext

}