package io.zeebe.bpmnspec.api.verifications

import io.zeebe.bpmnspec.api.TestRunner
import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState

class WorkflowInstanceStateVerification(
        val state: WorkflowInstanceState,
        val bpmnProcessId: String?
) : Verification {

    override fun verify(runner: TestRunner, context: WorkflowInstanceContext): VerificationResult {
        val actualState = runner.getWorkflowInstanceState(context)

        return if (actualState == state) {
            VerificationResult(isFulfilled = true)
        } else {
            VerificationResult(
                    isFulfilled = false,
                    failureMessage = "Expected the workflow instance to be in state '$state' but was '$actualState'."
            )
        }
    }
}