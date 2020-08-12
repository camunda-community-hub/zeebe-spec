package io.zeebe.bpmnspec.api

data class VerificationResult(
        val isFulfilled: Boolean,
        val failureMessage: String?
)