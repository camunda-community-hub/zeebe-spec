package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.*
import io.camunda.zeebe.client.api.worker.JobWorker
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration

class ZeebeTestRunner(
        private val environment: ZeebeEnvironment = ZeebeEnvironment(),
        private val reuseEnvironment: Boolean = false,
        private val beforeEachCallback: (ZeebeTestContext) -> Unit = {},
        private val afterEachCallback: (ZeebeTestContext) -> Unit = {}
) : TestRunner {

    private val logger = LoggerFactory.getLogger(ZeebeTestRunner::class.java)

    private val jobWorkers = mutableListOf<JobWorker>()

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
        jobWorkers.map(JobWorker::close)

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

    override fun deployWorkflow(name: String, bpmnXml: InputStream) {
        logger.debug("Deploying a BPMN. [name: {}]", name)

        environment.zeebeClient.newDeployCommand()
                .addResourceStream(bpmnXml, name)
                .send()
                .join()
    }

    override fun createWorkflowInstance(bpmnProcessId: String, variables: String): ProcessInstanceContext {
        logger.debug("Creating a workflow instance. [BPMN-process-id: {}, variables: {}]", bpmnProcessId, variables)

        val response = environment.zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion().variables(variables)
                .send()
                .join()

        return ZeebeProcessInstanceContext(
                processInstanceKey = response.processInstanceKey
        )
    }

    override fun completeTask(jobType: String, variables: String) {
        logger.debug("Starting a job worker to complete jobs. [job-type: {}, variables: {}]", jobType, variables)

        val jobWorker = environment.zeebeClient.newWorker()
                .jobType(jobType)
                .handler { jobClient, job ->
                    jobClient.newCompleteCommand(job.key)
                            .variables(variables)
                            .send()
                            .join()
                }
                .timeout(Duration.ofSeconds(1))
                .open()

        jobWorkers.add(jobWorker)
    }

    override fun publishMessage(messageName: String, correlationKey: String, variables: String) {
        logger.debug("Publishing a message. [name: {}, correlation-key: {}, variables: {}]",
                messageName, correlationKey, variables)

        environment.zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationKey)
                .variables(variables)
                .timeToLive(Duration.ofSeconds(10))
                .send()
                .join()
    }

    override fun throwError(jobType: String, errorCode: String, errorMessage: String) {
        logger.debug("Starting a job worker to throw errors. [job-type: {}, error-code: {}, error-message: {}]",
                jobType, errorCode, errorMessage)

        val jobWorker = environment.zeebeClient.newWorker()
                .jobType(jobType)
                .handler { jobClient, job ->
                    jobClient.newThrowErrorCommand(job.key)
                            .errorCode(errorCode)
                            .errorMessage(errorMessage)
                            .send()
                            .join()
                }
                .timeout(Duration.ofSeconds(1))
                .open()

        jobWorkers.add(jobWorker)
    }

    override fun cancelWorkflowInstance(context: ProcessInstanceContext) {
        val wfContext = context as ZeebeProcessInstanceContext

        logger.debug("Cancelling a workflow instance. [key: {}]", wfContext.processInstanceKey)

        environment.zeebeClient.newCancelInstanceCommand(wfContext.processInstanceKey)
                .send()
                .join()
    }

    override fun getWorkflowInstanceContexts(): List<ProcessInstanceContext> {

        return environment.zeeqsClient.getProcessInstanceKeys()
                .map { ZeebeProcessInstanceContext(processInstanceKey = it) }
    }

    override fun getWorkflowInstanceState(context: ProcessInstanceContext): WorkflowInstanceState {
        val wfContext = context as ZeebeProcessInstanceContext

        val state = environment.zeeqsClient.getProcessInstanceState(wfContext.processInstanceKey)
        return when (state) {
            "COMPLETED" -> WorkflowInstanceState.COMPLETED
            "TERMINATED" -> WorkflowInstanceState.TERMINATED
            "ACTIVATED" -> WorkflowInstanceState.ACTIVATED
            else -> WorkflowInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(context: ProcessInstanceContext): List<ElementInstance> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeeqsClient.getElementInstances(processInstanceKey = wfContext.processInstanceKey).map {
            ElementInstance(
                    elementId = it.elementId,
                    elementName = it.elementName,
                    state = when (it.state) {
                        "ACTIVATED" -> ElementInstanceState.ACTIVATED
                        "COMPLETED" -> ElementInstanceState.COMPLETED
                        "TERMINATED" -> ElementInstanceState.TERMINATED
                        "TAKEN" -> ElementInstanceState.TAKEN
                        else -> ElementInstanceState.UNKNOWN
                    }
            )
        }
    }

    override fun getWorkflowInstanceVariables(context: ProcessInstanceContext): List<WorkflowInstanceVariable> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeeqsClient.getWorkflowInstanceVariables(processInstanceKey = wfContext.processInstanceKey)
                .map {
                    WorkflowInstanceVariable(
                            variableName = it.name,
                            variableValue = it.value,
                            scopeElementId = it.scope?.elementId ?: "",
                            scopeElementName = it.scope?.elementName ?: ""
                    )
                }
    }

    override fun getIncidents(context: ProcessInstanceContext): List<Incident> {
        val wfContext = context as ZeebeProcessInstanceContext

        return environment.zeeqsClient.getIncidents(processInstanceKey = wfContext.processInstanceKey)
                .map {
                    Incident(
                            errorType = it.errorType,
                            errorMessage = it.errorMessage,
                            state = when (it.state) {
                                "CREATED" -> IncidentState.CREATED
                                "RESOLVED" -> IncidentState.RESOLVED
                                else -> IncidentState.UNKNOWN
                            },
                            elementId = it.elementInstance?.elementId ?: "",
                            elementName = it.elementInstance?.elementName ?: ""
                    )
                }
    }

}