package io.zeebe.bpmnspec.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.TestContext
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.api.WorkflowInstanceContext

class CompleteTaskAction(
        val jobType: String,
        val variables: String
) : Action {

    override fun execute(runner: TestRunner, testContext: TestContext) {
        runner.completeTask(jobType, variables)
    }
}