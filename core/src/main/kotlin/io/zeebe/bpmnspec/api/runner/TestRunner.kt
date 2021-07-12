package io.zeebe.bpmnspec.api.runner

import io.zeebe.bpmnspec.api.ProcessInstanceContext
import java.io.InputStream

interface TestRunner {

    fun beforeAll()

    fun beforeEach()

    fun afterEach()

    fun afterAll()

    fun deployProcess(name: String, bpmnXml: InputStream)

    fun createProcessInstance(bpmnProcessId: String, variables: String): ProcessInstanceContext

    fun completeTask(jobType: String, variables: String)

    fun publishMessage(messageName: String, correlationKey: String, variables: String)

    fun throwError(jobType: String, errorCode: String, errorMessage: String)

    fun cancelProcessInstance(context: ProcessInstanceContext)

    fun getProcessInstanceContexts(): List<ProcessInstanceContext>

    fun getProcessInstanceState(context: ProcessInstanceContext): ProcessInstanceState

    fun getElementInstances(context: ProcessInstanceContext): List<ElementInstance>

    fun getProcessInstanceVariables(context: ProcessInstanceContext): List<ProcessInstanceVariable>

    fun getIncidents(context: ProcessInstanceContext): List<Incident>

}