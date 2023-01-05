package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.ProcessInstanceKey
import java.time.Duration

data class TestContext(
    val storeContext: (String, ProcessInstanceKey) -> Unit,
    val getContext: (String?) -> ProcessInstanceKey,
    val verificationTimeout: Duration,
    val verificationRetryInterval: Duration
)