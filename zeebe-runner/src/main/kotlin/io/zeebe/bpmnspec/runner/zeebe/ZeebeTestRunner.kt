package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.*
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration

class ZeebeTestRunner(
        private val reuseEnvironment: Boolean = false
) : TestRunner {

    private val logger = LoggerFactory.getLogger(ZeebeTestRunner::class.java)

    private val environment = ZeebeEnvironment()

    override fun beforeAll() {
        if (reuseEnvironment) {
            environment.setup()
        }
    }

    override fun beforeEach() {
        if (!reuseEnvironment) {
            environment.setup()
        }
    }

    override fun afterEach() {
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

        environment.client.newDeployCommand()
                .addResourceStream(bpmnXml, name)
                .send()
                .join()
    }

    override fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext {
        logger.debug("Creating a workflow instance. [BPMN-process-id: {}, variables: {}]", bpmnProcessId, variables)

        val response = environment.client.newCreateInstanceCommand()
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

        environment.client.newWorker()
                .jobType(jobType)
                .handler { jobClient, job ->
                    jobClient.newCompleteCommand(job.key)
                            .variables(variables)
                            .send()
                            .join()
                }
                .timeout(Duration.ofSeconds(1))
                .open()
    }

    override fun publishMessage(messageName: String, correlationKey: String, variables: String) {
        logger.debug("Publishing a message. [name: {}, correlation-key: {}, variables: {}]",
                messageName, correlationKey, variables)

        environment.client.newPublishMessageCommand()
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

        environment.client.newWorker()
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
    }

    override fun cancelWorkflowInstance(context: WorkflowInstanceContext) {
        val wfContext = context as ZeebeWorkflowInstanceContext

        logger.debug("Cancelling a workflow instance. [key: {}]", wfContext.workflowInstanceKey)

        environment.client.newCancelInstanceCommand(wfContext.workflowInstanceKey)
                .send()
                .join()
    }

    override fun getWorkflowInstanceContexts(): List<WorkflowInstanceContext> {

        return environment.zeeqs.getWorkflowInstanceKeys()
                .map { ZeebeWorkflowInstanceContext(workflowInstanceKey = it) }
    }

    override fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState {
        val wfContext = context as ZeebeWorkflowInstanceContext

        val state = environment.zeeqs.getWorkflowInstanceState(wfContext.workflowInstanceKey)
        return when (state) {
            "COMPLETED" -> WorkflowInstanceState.COMPLETED
            "TERMINATED" -> WorkflowInstanceState.TERMINATED
            "ACTIVATED" -> WorkflowInstanceState.ACTIVATED
            else -> WorkflowInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(context: WorkflowInstanceContext): List<ElementInstance> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqs.getElementInstances(workflowInstanceKey = wfContext.workflowInstanceKey).map {
            ElementInstance(
                    elementId = it.elementId,
                    elementName = it.elementName,
                    state = when (it.state) {
                        "ACTIVATED" -> ElementInstanceState.ACTIVATED
                        "COMPLETED" -> ElementInstanceState.COMPLETED
                        "TERMINATED" -> ElementInstanceState.TERMINATED
                        else -> ElementInstanceState.UNKNOWN
                    }
            )
        }
    }

    override fun getWorkflowInstanceVariables(context: WorkflowInstanceContext): List<WorkflowInstanceVariable> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqs.getWorkflowInstanceVariables(workflowInstanceKey = wfContext.workflowInstanceKey)
                .map {
                    WorkflowInstanceVariable(
                            variableName = it.name,
                            variableValue = it.value,
                            scopeElementId = it.scope.elementId,
                            scopeElementName = it.scope.elementName
                    )
                }
    }

    override fun getIncidents(context: WorkflowInstanceContext): List<Incident> {
        val wfContext = context as ZeebeWorkflowInstanceContext

        return environment.zeeqs.getIncidents(workflowInstanceKey = wfContext.workflowInstanceKey)
                .map {
                    Incident(
                            errorType = it.errorType,
                            errorMessage = it.errorMessage,
                            state = when (it.state) {
                                "CREATED" -> IncidentState.CREATED
                                "RESOLVED" -> IncidentState.RESOLVED
                                else -> IncidentState.UNKNOWN
                            },
                            elementId = it.elementInstance.elementId,
                            elementName = it.elementInstance.elementName
                    )
                }
    }

}