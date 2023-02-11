package org.camunda.community.zeebe.spec.api

import io.camunda.zeebe.client.ZeebeClient

interface SpecTestRunnerContext {

    fun getZeebeClient(): ZeebeClient

}