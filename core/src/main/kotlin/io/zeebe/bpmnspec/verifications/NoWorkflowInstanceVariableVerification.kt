package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.runner.SpecStateProvider

class NoProcessInstanceVariableVerification(
    val variableName: String,
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

        val actualVariable = stateProvider.getProcessInstanceVariables(context)
            .filter { it.variableName == variableName }
            .filter { variable ->
                scopeElementId?.let { it == variable.scopeElementId } ?: true
            }
            .filter { variable ->
                scopeElementName?.let { it == variable.scopeElementName } ?: true
            }
            .firstOrNull()

        val wfAlias = processInstance?.let { "of the process instance '$it'" } ?: ""
        val element = scopeElementId?.let { "of the scope with id '$it'" }
            ?: scopeElementName?.let { "of the scope with name '$it'" }
            ?: ""

        return actualVariable?.let {
            VerificationResult(
                isFulfilled = false,
                failureMessage = "Expected no variable with name '$variableName' $element $wfAlias to be created but was created with the value '${it.variableValue}'."
            )
        } ?: VerificationResult(isFulfilled = true)
    }
}