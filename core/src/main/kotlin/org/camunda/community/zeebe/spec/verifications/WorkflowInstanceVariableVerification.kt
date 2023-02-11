package org.camunda.community.zeebe.spec.verifications

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.Verification
import org.camunda.community.zeebe.spec.api.VerificationResult
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

class ProcessInstanceVariableVerification(
    val variableName: String,
    val variableValue: String,
    val processInstance: String?,
    val scopeElementId: String?,
    val scopeElementName: String?
) : Verification {

    override fun verify(
        stateProvider: SpecStateProvider,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult {

        val context = processInstance?.let { contexts[processInstance] }
            ?: contexts.values.first()

        val actualVariableValue = stateProvider.getProcessInstanceVariables(context)
            .filter { it.variableName == variableName }
            .filter { variable ->
                scopeElementId?.let { it == variable.scopeElementId } ?: true
            }
            .filter { variable ->
                scopeElementName?.let { it == variable.scopeElementName } ?: true
            }
            .firstOrNull()
            ?.variableValue

        val wfAlias = processInstance?.let { "of the process instance '$it'" } ?: ""
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