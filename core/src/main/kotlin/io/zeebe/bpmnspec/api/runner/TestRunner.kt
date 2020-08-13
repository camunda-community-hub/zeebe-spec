package io.zeebe.bpmnspec.api.runner

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import java.io.InputStream

interface TestRunner {

    fun init()

    fun cleanUp()

    fun deployWorkflow(name: String, bpmnXml: InputStream)

    fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext

    fun completeTask(jobType: String, variables: String)

    fun getWorkflowInstanceContexts(): List<WorkflowInstanceContext>

    fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState

}