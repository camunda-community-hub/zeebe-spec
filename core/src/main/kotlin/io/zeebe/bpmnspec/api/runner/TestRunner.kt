package io.zeebe.bpmnspec.api.runner

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import java.io.InputStream
import java.time.Duration

interface TestRunner {

    fun init()

    fun cleanUp()

    fun deployWorkflow(name: String, bpmnXml: InputStream)

    fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext

    fun completeTask(jobType: String, variables: String)

    fun publishMessage(messageName: String, correlationKey: String, variables: String)

    fun throwError(jobType: String, errorCode: String, errorMessage: String)

    fun cancelWorkflowInstance(context: WorkflowInstanceContext)

    fun getWorkflowInstanceContexts(): List<WorkflowInstanceContext>

    fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState

}