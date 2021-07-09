package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.TestRunner
import java.time.Duration
import java.time.Instant

class AwaitElementInstanceStateAction(
        val state: ElementInstanceState,
        val elementId: String?,
        val elementName: String?,
        val workflowInstance: String?) : Action {

    override fun execute(runner: TestRunner, testContext: TestContext) {
        val context = testContext.getContext(workflowInstance)

        val start = Instant.now()

        do {
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

            val shouldRetry = actualState?.let { it != state } ?: true &&
                    Duration.between(start, Instant.now()).minus(testContext.verificationTimeout).isNegative

            if (shouldRetry) {
                Thread.sleep(testContext.verificationRetryInterval.toMillis())
            }
        } while (shouldRetry)

    }
}