package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState

class WorkflowInstanceStateVerification(
        val state: WorkflowInstanceState,
        val workflowInstance: String?
) : Verification {

    override fun verify(runner: TestRunner, contexts: Map<String, ProcessInstanceContext>): VerificationResult {

        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val actualState = runner.getWorkflowInstanceState(context)

        return if (actualState == state) {
            VerificationResult(isFulfilled = true)
        } else {
            val alias = workflowInstance?.let { "'$it'" } ?: ""
            VerificationResult(
                    isFulfilled = false,
                    failureMessage = "Expected the workflow instance $alias to be in state '$state' but was '$actualState'."
            )
        }
    }
}