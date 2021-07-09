package io.zeebe.bpmnspec.verifications

import io.zeebe.bpmnspec.api.Verification
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.IncidentState
import io.zeebe.bpmnspec.api.runner.TestRunner

class IncidentStateVerification(
        val state: IncidentState,
        val errorType: String,
        val errorMessage: String?,
        val elementId: String?,
        val elementName: String?,
        val workflowInstance: String?
) : Verification {

    override fun verify(runner: TestRunner, contexts: Map<String, ProcessInstanceContext>): VerificationResult {

        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val actualIncident = runner.getIncidents(context)
                .filter { it.errorType == errorType }
                .filter { incident ->
                    elementId?.let { it == incident.elementId } ?: true
                }
                .filter { incident ->
                    elementName?.let { it == incident.elementName } ?: true
                }
                .filter { incident ->
                    errorMessage?.let { it == incident.errorMessage } ?: true
                }
                .firstOrNull()

        val wfAlias = workflowInstance?.let { "of the workflow instance '$it'" } ?: ""
        val element = elementId?.let { "with id '$it'" }
                ?: elementName?.let { "with name '$it'" }
                ?: ""
        val incident = "with error-type '$errorType'" + errorMessage?.let { " and error-message '$it'" }

        return actualIncident?.let {
            if (it.state == state) {
                VerificationResult(isFulfilled = true)
            } else {
                VerificationResult(
                        isFulfilled = false,
                        failureMessage = "Expected the incident $incident $element $wfAlias to be in state '$state' but was '${it.state}'."
                )
            }
        } ?: VerificationResult(
                isFulfilled = false,
                failureMessage = "Expected an incident $incident $element $wfAlias to be in state '$state' but no incident found."
        )
    }
}