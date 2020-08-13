package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.api.WorkflowInstanceContext

class CompleteTaskAction(
        val jobType: String,
        val variables: String
) : Action {

    override fun execute(runner: TestRunner, contexts: MutableMap<String, WorkflowInstanceContext>) {
        runner.completeTask(jobType, variables)
    }
}