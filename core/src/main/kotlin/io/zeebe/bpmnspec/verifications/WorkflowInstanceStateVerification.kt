package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState

class ProcessInstanceStateVerification(
    val state: ProcessInstanceState,
    val processInstance: String?
) : Verification {

    override fun verify(
        runner: TestRunner,
        contexts: Map<String, ProcessInstanceContext>
    ): VerificationResult {

        val context = processInstance?.let { contexts[processInstance] }
            ?: contexts.values.first()

        val actualState = runner.getProcessInstanceState(context)

        return if (actualState == state) {
            VerificationResult(isFulfilled = true)
        } else {
            val alias = processInstance?.let { "'$it'" } ?: ""
            VerificationResult(
                isFulfilled = false,
                failureMessage = "Expected the process instance $alias to be in state '$state' but was '$actualState'."
            )
        }
    }
}