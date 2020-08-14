package io.zeebe.bpmnspec.api.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.TestRunner

class ElementInstanceStateVerification(
        val state: ElementInstanceState,
        val elementId: String?,
        val elementName: String?,
        val workflowInstance: String?
) : Verification {

    override fun verify(runner: TestRunner, contexts: Map<String, WorkflowInstanceContext>): VerificationResult {

        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val actualState = runner.getElementInstanceState(
                context = context,
                elementId = elementId,
                elementName = elementName
        )

        return if (actualState == state) {
            VerificationResult(isFulfilled = true)
        } else {
            val wfAlias = workflowInstance?.let { "of the workflow instance '$it'" } ?: ""
            val element = elementId?.let { "with id '$it'" }
                    ?: elementName?.let { "with name '$it'" }
                    ?: ""

            VerificationResult(
                    isFulfilled = false,
                    failureMessage = "Expected the element $element $wfAlias to be in state '$state' but was '$actualState'."
            )
        }
    }
}