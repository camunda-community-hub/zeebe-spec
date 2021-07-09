package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.api.runner.TestRunner

interface Verification {

    fun verify(runner: TestRunner, contexts: Map<String, ProcessInstanceContext>): VerificationResult

}