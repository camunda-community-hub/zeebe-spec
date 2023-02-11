package org.camunda.community.zeebe.spec.verifications

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.Verification
import org.camunda.community.zeebe.spec.api.VerificationResult
import org.camunda.community.zeebe.spec.api.dto.ElementInstanceState
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

class ElementInstanceStateVerification(
    val state: ElementInstanceState,
    val elementId: String?,
    val elementName: String?,
    val processInstance: String?
) : Verification {

    override fun verify(
        stateProvider: SpecStateProvider,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult {

        val context = processInstance?.let { contexts[processInstance] }
            ?: contexts.values.first()

        val actualState =
            stateProvider.getElementInstances(context)
                .filter { elementInstance ->
                    elementId?.let { it == elementInstance.elementId } ?: true
                }
                .filter { elementInstance ->
                    elementName?.let { it == elementInstance.elementName } ?: true
                }
                .firstOrNull()
                ?.state

        val wfAlias = processInstance?.let { "of the process instance '$it'" } ?: ""
        val element = elementId?.let { "with id '$it'" }
            ?: elementName?.let { "with name '$it'" }
            ?: ""

        return actualState?.let {
            if (it == state) {
                VerificationResult(isFulfilled = true)
            } else {
                VerificationResult(
                    isFulfilled = false,
                    failureMessage = "Expected the element $element $wfAlias to be in state '$state' but was '$actualState'."
                )
            }
        } ?: VerificationResult(
            isFulfilled = false,
            failureMessage = "Expected the element $element $wfAlias to be in state '$state' but no element instance found."
        )
    }
}