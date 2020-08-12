package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import java.io.InputStream

interface TestRunner {

    fun init()

    fun cleanUp()

    fun deployWorkflow(name: String, bpmnXml: InputStream)

    fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext

    fun completeTask(jobType: String, variables: String)

    fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState

}