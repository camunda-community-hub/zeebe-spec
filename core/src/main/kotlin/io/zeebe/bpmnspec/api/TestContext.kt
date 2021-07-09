package io.zeebe.bpmnspec.api

import java.time.Duration

data class TestContext(
        val storeContext: (String, ProcessInstanceContext) -> Unit,
        val getContext: (String?) -> ProcessInstanceContext,
        val verificationTimeout: Duration,
        val verificationRetryInterval: Duration
)