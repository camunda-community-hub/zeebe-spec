package io.zeebe.bpmnspec.api

interface Verification {

    fun verify(runner: TestRunner, context: WorkflowInstanceContext): VerificationResult

}