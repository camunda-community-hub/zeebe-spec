package io.zeebe.bpmnspec.runner.zeebe

import io.camunda.zeebe.client.ZeebeClient

data class ZeebeTestContext(
        val zeebeClient: ZeebeClient
)