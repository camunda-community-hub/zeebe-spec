package org.camunda.community.zeebe.spec.api

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import java.time.Duration

data class TestContext(
    val storeContext: (String, ProcessInstanceKey) -> Unit,
    val getContext: (String?) -> ProcessInstanceKey,
    val verificationTimeout: Duration,
    val verificationRetryInterval: Duration
)