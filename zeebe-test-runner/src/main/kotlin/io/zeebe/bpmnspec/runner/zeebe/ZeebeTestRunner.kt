package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.*
import io.zeebe.client.api.worker.JobWorker
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration
import java.util.function.Consumer

class ZeebeTestRunner(
        private val reuseEnvironment: Boolean = false,
        private val beforeEachCallback: (ZeebeTestContext) -> Unit = {},
        private val afterEachCallback: (ZeebeTestContext) -> Unit = {}
) : TestRunner {

    private val logger = LoggerFactory.getLogger(ZeebeTestRunner::class.java)

    private val environment = ZeebeEnvironment()
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

    override fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext {
        logger.debug("Creating a workflow instance. [BPMN-process-id: {}, variables: {}]", bpmnProcessId, variables)

        val response = environment.zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion().variables(variables)
                .send()
                .join()

        return ZeebeWorkflowInstanceContext(
                workflowInstanceKey = response.workflowInstanceKey
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

    override fun cancelWorkflowInstance(context: WorkflowInstanceContext) {
        val wfContext = context as ZeebeWorkflowInstanceContext

        logger.debug("Cancelling a workflow instance. [key: {}]", wfContext.workflowInstanceKey)

        environment.zeebeClient.newCancelInstanceCommand(wfContext.workflowInstanceKey)
                .send()
                .join()
    }

    override fun getWorkflowInstanceContexts(): List<WorkflowInstanceContext> {

        return environment.zeeqsClient.getWorkflowInstanceKeys()
                .map { ZeebeWorkflowInstanceContext(workflowInstanceKey = it) }
    }

    override fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState {
        val wfContext = context as ZeebeWorkflowInstanceContext

        val state = environment.zeeqsClient.getWorkflowInstanceState(wfContext.workflowInstanceKey)
        return when (state) {
            "COMPLETED" -> WorkflowInstanceState.COMPLETED
            "TERMINATED" -> WorkflowInstanceState.TERMINATED
            "ACTIVATED" -> WorkflowInstanceState.ACTIVATED
            else -> WorkflowInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(context: WorkflowInstanceContext): List<ElementInstance> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqsClient.getElementInstances(workflowInstanceKey = wfContext.workflowInstanceKey).map {
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

    override fun getWorkflowInstanceVariables(context: WorkflowInstanceContext): List<WorkflowInstanceVariable> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqsClient.getWorkflowInstanceVariables(workflowInstanceKey = wfContext.workflowInstanceKey)
                .map {
                    WorkflowInstanceVariable(
                            variableName = it.name,
                            variableValue = it.value,
                            scopeElementId = it.scope?.elementId ?: "",
                            scopeElementName = it.scope?.elementName ?: ""
                    )
                }
    }

    override fun getIncidents(context: WorkflowInstanceContext): List<Incident> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqsClient.getIncidents(workflowInstanceKey = wfContext.workflowInstanceKey)
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