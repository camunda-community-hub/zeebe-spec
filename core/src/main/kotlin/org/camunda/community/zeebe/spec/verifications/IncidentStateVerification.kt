package org.camunda.community.zeebe.spec.verifications

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.api.Verification
import org.camunda.community.zeebe.spec.api.VerificationResult
import org.camunda.community.zeebe.spec.api.dto.IncidentState
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

class IncidentStateVerification(
    val state: IncidentState,
    val errorType: String,
    val errorMessage: String?,
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

        val actualIncident = stateProvider.getIncidents(context)
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

        val wfAlias = processInstance?.let { "of the process instance '$it'" } ?: ""
        val element = elementId?.let { "with id '$it'" }
            ?: elementName?.let { "with name '$it'" }
            ?: ""
        val incident =
            "with error-type '$errorType'" + errorMessage?.let { " and error-message '$it'" }

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