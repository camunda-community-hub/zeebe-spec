package io.zeebe.bpmnspec.api

import java.time.Duration

data class TestContext(
        val storeContext: (String, WorkflowInstanceContext) -> Unit,
        val getContext: (String?) -> WorkflowInstanceContext,
        val verificationTimeout: Duration,
        val verificationRetryInterval: Duration
)