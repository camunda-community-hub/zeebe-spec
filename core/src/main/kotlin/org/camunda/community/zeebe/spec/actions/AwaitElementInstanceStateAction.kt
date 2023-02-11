package org.camunda.community.zeebe.spec.actions

import org.camunda.community.zeebe.spec.api.Action
import org.camunda.community.zeebe.spec.api.TestContext
import org.camunda.community.zeebe.spec.api.dto.ElementInstanceState
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.camunda.community.zeebe.spec.runner.SpecStateProvider
import java.time.Duration
import java.time.Instant

class AwaitElementInstanceStateAction(
    val state: ElementInstanceState,
    val elementId: String?,
    val elementName: String?,
    val processInstance: String?
) : Action {

    override fun execute(
        actionExecutor: SpecActionExecutor,
        stateProvider: SpecStateProvider,
        testContext: TestContext
    ) {
        val context = testContext.getContext(processInstance)

        val start = Instant.now()

        do {
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

            val shouldRetry = actualState?.let { it != state } ?: true &&
                    Duration.between(start, Instant.now())
                        .minus(testContext.verificationTimeout).isNegative

            if (shouldRetry) {
                Thread.sleep(testContext.verificationRetryInterval.toMillis())
            }
        } while (shouldRetry)

    }
}