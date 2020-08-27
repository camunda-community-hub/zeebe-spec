package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.client.ZeebeClient

data class ZeebeTestContext(
        val zeebeClient: ZeebeClient
)