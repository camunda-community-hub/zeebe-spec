package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.api.runner.TestRunner

interface Action {

    fun execute(runner: TestRunner): Pair<String, WorkflowInstanceContext>?

}