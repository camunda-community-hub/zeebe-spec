package io.zeebe.bpmnspec.api.runner

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import java.io.InputStream

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

    fun getElementInstanceStateById(context: WorkflowInstanceContext, elementId: String): ElementInstanceState

    fun getElementInstanceStateByName(context: WorkflowInstanceContext, elementName: String): ElementInstanceState

    fun getWorkflowInstanceVariables(context: WorkflowInstanceContext): List<WorkflowInstanceVariable>

}