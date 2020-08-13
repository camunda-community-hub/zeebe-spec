package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class ThrowErrorAction(
        val jobType: String,
        val errorCode: String,
        val errorMessage: String
) : Action {

    override fun execute(runner: TestRunner): Pair<String, WorkflowInstanceContext>? {
        runner.throwError(
                jobType = jobType,
                errorCode = errorCode,
                errorMessage = errorMessage)
        return null
    }
}