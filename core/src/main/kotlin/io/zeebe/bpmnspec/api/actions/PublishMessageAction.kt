package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner
import java.time.Duration

class PublishMessageAction(
        val messageName: String,
        val correlationKey: String,
        val variables: String
): Action {

    override fun execute(runner: TestRunner): Pair<String, WorkflowInstanceContext>? {
        runner.publishMessage(
                messageName = messageName,
                correlationKey = correlationKey,
                variables = variables
        )
        return null
    }
}