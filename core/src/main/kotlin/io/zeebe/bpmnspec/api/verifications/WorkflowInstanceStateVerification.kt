package io.zeebe.bpmnspec.api.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState

data class WorkflowInstanceStateVerification(
        val state: WorkflowInstanceState,
        val bpmnProcessId: String?
) : Verification