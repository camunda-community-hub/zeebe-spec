package io.zeebe.bpmnspec.api.runner

import io.zeebe.bpmnspec.api.ProcessInstanceContext
import java.io.InputStream

interface TestRunner {

    fun beforeAll()

    fun beforeEach()

    fun afterEach()

    fun afterAll()

    fun deployWorkflow(name: String, bpmnXml: InputStream)

    fun createWorkflowInstance(bpmnProcessId: String, variables: String): ProcessInstanceContext

    fun completeTask(jobType: String, variables: String)

    fun publishMessage(messageName: String, correlationKey: String, variables: String)

    fun throwError(jobType: String, errorCode: String, errorMessage: String)

    fun cancelWorkflowInstance(context: ProcessInstanceContext)

    fun getWorkflowInstanceContexts(): List<ProcessInstanceContext>

    fun getWorkflowInstanceState(context: ProcessInstanceContext): WorkflowInstanceState

    fun getElementInstances(context: ProcessInstanceContext): List<ElementInstance>

    fun getWorkflowInstanceVariables(context: ProcessInstanceContext): List<WorkflowInstanceVariable>

    fun getIncidents(context: ProcessInstanceContext): List<Incident>

}