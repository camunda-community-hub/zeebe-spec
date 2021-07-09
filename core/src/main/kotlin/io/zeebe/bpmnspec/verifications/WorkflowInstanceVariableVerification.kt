package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class WorkflowInstanceVariableVerification(
        val variableName: String,
        val variableValue: String,
        val workflowInstance: String?,
        val scopeElementId: String?,
        val scopeElementName: String?
) : Verification {

    override fun verify(runner: TestRunner, contexts: Map<String, ProcessInstanceContext>): VerificationResult {

        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val actualVariableValue = runner.getWorkflowInstanceVariables(context)
                .filter { it.variableName == variableName }
                .filter { variable ->
                    scopeElementId?.let { it == variable.scopeElementId } ?: true
                }
                .filter { variable ->
                    scopeElementName?.let { it == variable.scopeElementName } ?: true
                }
                .firstOrNull()
                ?.variableValue

        val wfAlias = workflowInstance?.let { "of the workflow instance '$it'" } ?: ""
        val element = scopeElementId?.let { "of the scope with id '$it'" }
                ?: scopeElementName?.let { "of the scope with name '$it'" }
                ?: ""

        return actualVariableValue?.let {
            if (actualVariableValue == variableValue) {
                VerificationResult(isFulfilled = true)
            } else
                VerificationResult(
                        isFulfilled = false,
                        failureMessage = "Expected the variable with name '$variableName' $element $wfAlias to have the value '$variableValue' but was '$actualVariableValue'."
                )
        } ?: VerificationResult(
                isFulfilled = false,
                failureMessage = "Expected the variable with name '$variableName' $element $wfAlias to have the value '$variableValue' but no variable found."
        )

    }
}