package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.client.ZeebeClient

data class EzeTestContext(
    val zeebeClient: ZeebeClient
)