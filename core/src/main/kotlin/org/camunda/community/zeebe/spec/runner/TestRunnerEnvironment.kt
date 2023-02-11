package org.camunda.community.zeebe.spec.runner

import org.camunda.community.zeebe.spec.api.SpecTestRunnerContext

interface TestRunnerEnvironment : AutoCloseable {

    val actionExecutor: SpecActionExecutor
    val stateProvider: SpecStateProvider

    fun create()

    fun getContext(): SpecTestRunnerContext

}