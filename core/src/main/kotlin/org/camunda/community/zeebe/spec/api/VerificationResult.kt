package org.camunda.community.zeebe.spec.api

data class VerificationResult(
    val isFulfilled: Boolean,
    val failureMessage: String = ""
)