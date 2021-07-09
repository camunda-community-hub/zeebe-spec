package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.TestRunner

class ElementInstanceStateVerification(
        val state: ElementInstanceState,
        val elementId: String?,
        val elementName: String?,
        val workflowInstance: String?
) : Verification {

    override fun verify(runner: TestRunner, contexts: Map<String, ProcessInstanceContext>): VerificationResult {

        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val actualState =
                runner.getElementInstances(context)
                        .filter { elementInstance ->
                            elementId?.let { it == elementInstance.elementId } ?: true
                        }
                        .filter { elementInstance ->
                            elementName?.let { it == elementInstance.elementName } ?: true
                        }
                        .firstOrNull()
                        ?.state

        val wfAlias = workflowInstance?.let { "of the workflow instance '$it'" } ?: ""
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