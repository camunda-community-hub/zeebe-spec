package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class PublishMessageAction(
        val messageName: String,
        val correlationKey: String,
        val variables: String
): Action {

    override fun execute(runner: TestRunner, testContext: TestContext) {
        runner.publishMessage(
                messageName = messageName,
                correlationKey = correlationKey,
                variables = variables
        )
    }
}