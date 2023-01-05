package io.zeebe.bpmnspec.api

import io.camunda.zeebe.client.ZeebeClient

interface SpecTestRunnerContext {

    fun getZeebeClient(): ZeebeClient

}