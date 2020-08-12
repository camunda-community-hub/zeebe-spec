package io.zeebe.bpmnspec.api.verifications

import io.zeebe.bpmnspec.api.Verification

data class WorkflowInstanceCompletedVerification(
        val bpmnProcessId: String
): Verification