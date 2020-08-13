package io.zeebe.bpmnspec.api

interface Action {

    fun execute(runner: TestRunner): WorkflowInstanceContext?

}