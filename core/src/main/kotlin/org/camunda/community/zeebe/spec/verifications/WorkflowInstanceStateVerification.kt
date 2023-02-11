package org.camunda.community.zeebe.spec.verifications

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.Verification
import org.camunda.community.zeebe.spec.api.VerificationResult
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceState
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

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