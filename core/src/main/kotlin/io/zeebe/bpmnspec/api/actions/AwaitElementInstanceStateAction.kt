package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.VerificationResult
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.TestRunner
import java.time.Duration
import java.time.Instant

class AwaitElementInstanceStateAction(
        val state: ElementInstanceState,
        val elementId: String?,
        val elementName: String?,
        val workflowInstance: String?): Action {

    // TODO (saig0): take from runner configuration
    val timeout = Duration.ofSeconds(10)
    val retryInterval = Duration.ofMillis(10)

    override fun execute(runner: TestRunner, contexts: MutableMap<String, WorkflowInstanceContext>) {
        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        val start = Instant.now()

        do {
            val actualState = runner.getElementInstanceState(
                    context = context,
                    elementId = elementId,
                    elementName = elementName
            )

            val shouldRetry = actualState != state &&
                    Duration.between(start, Instant.now()).minus(timeout).isNegative

            if (shouldRetry) {
                Thread.sleep(retryInterval.toMillis())
            }
        } while (shouldRetry)

    }
}