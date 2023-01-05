package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.dto.ProcessInstanceState
import io.zeebe.bpmnspec.runner.SpecStateProvider

class ProcessInstanceStateVerification(
    val state: ProcessInstanceState,
    val processInstance: String?
) : Verification {

    override fun verify(
        stateProvider: SpecStateProvider,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult {

        val context = processInstance?.let { contexts[processInstance] }
            ?: contexts.values.first()

        val actualState = stateProvider.getProcessInstanceState(context)

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