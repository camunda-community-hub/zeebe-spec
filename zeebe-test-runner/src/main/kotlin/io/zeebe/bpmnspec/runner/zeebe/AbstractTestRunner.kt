package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.ElementInstance
import io.zeebe.bpmnspec.api.runner.Incident
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceVariable
import io.zeebe.bpmnspec.api.runner.TestRunner
import mu.KLogger
import java.io.InputStream

abstract class AbstractTestRunner(
    protected val environment: ZeebeEnvironment,
    protected val reuseEnvironment: Boolean,
    protected val beforeEachCallback: (ZeebeTestContext) -> Unit,
    protected val afterEachCallback: (ZeebeTestContext) -> Unit,
    protected val logger: KLogger
) : TestRunner {
    override fun beforeAll() {
        if (reuseEnvironment) {
            environment.setup()
        }
    }

    override fun beforeEach() {
        if (!reuseEnvironment || !environment.isRunning) {
            environment.setup()
        }
        val testContext = ZeebeTestContext(zeebeClient = environment.zeebeClient)
        beforeEachCallback(testContext)
    }

    override fun afterEach() {
        val testContext = ZeebeTestContext(zeebeClient = environment.zeebeClient)
        afterEachCallback(testContext)

        if (!reuseEnvironment) {
            environment.cleanUp()
        }
    }

    override fun afterAll() {
        if (reuseEnvironment) {
            environment.cleanUp()
        }
    }

    override fun deployProcess(name: String, bpmnXml: InputStream) {
        logger.debug { "Deploying a BPMN. [name: $name]" }

        environment.zeebeService.deployProcess(name, bpmnXml)
    }

    override fun createProcessInstance(
        bpmnProcessId: String,
        variables: String
    ): ProcessInstanceContext {
        logger.debug { "Creating a process instance. [BPMN-process-id: $bpmnProcessId, variables: $variables]" }

        val response = environment.zeebeService.createProcessInstance(bpmnProcessId, variables)

        return ZeebeProcessInstanceContext(processInstanceKey = response)
    }

    override fun completeTask(jobType: String, variables: String) {
        logger.debug { "Starting a job worker to complete jobs. [job-type: $jobType, variables: $variables]" }

        environment.zeebeService.completeTask(jobType, variables)
    }

    override fun publishMessage(messageName: String, correlationKey: String, variables: String) {
        logger.debug {
            "Publishing a message. [name: $messageName, correlation-key: $correlationKey, variables: $variables]"
        }

        environment.zeebeService.publishMessage(messageName, correlationKey, variables)
    }

    override fun throwError(jobType: String, errorCode: String, errorMessage: String) {
        logger.debug {
            "Starting a job worker to throw errors. [job-type: $jobType, error-code: $errorCode, error-message: $errorMessage]"
        }

        environment.zeebeService.throwError(jobType, errorCode, errorMessage)
    }

    override fun cancelProcessInstance(context: ProcessInstanceContext) {
        val wfContext = context as ZeebeProcessInstanceContext

        logger.debug {
            "Cancelling a process instance. [key: ${wfContext.processInstanceKey}]"
        }

        environment.zeebeService.cancelProcessInstance(wfContext.processInstanceKey)
    }

    override fun getProcessInstanceContexts(): List<ProcessInstanceContext> {
        return environment.zeebeEventRepository.getProcessInstanceKeys().map { ZeebeProcessInstanceContext(it) }
    }

    override fun getProcessInstanceState(context: ProcessInstanceContext): ProcessInstanceState {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeebeEventRepository.getProcessInstanceState(wfContext.processInstanceKey)
    }

    override fun getElementInstances(context: ProcessInstanceContext): List<ElementInstance> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeebeEventRepository.getElementInstances(wfContext.processInstanceKey)
    }

    override fun getProcessInstanceVariables(context: ProcessInstanceContext): List<ProcessInstanceVariable> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeebeEventRepository.getProcessInstanceVariables(wfContext.processInstanceKey)
    }

    override fun getIncidents(context: ProcessInstanceContext): List<Incident> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeebeEventRepository.getIncidents(wfContext.processInstanceKey)
    }
}
