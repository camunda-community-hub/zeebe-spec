package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.client.api.worker.JobWorker
import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.instance.FlowElement
import io.camunda.zeebe.protocol.record.intent.IncidentIntent
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent
import io.camunda.zeebe.protocol.record.value.BpmnElementType
import io.zeebe.bpmnspec.api.ProcessInstanceContext
import io.zeebe.bpmnspec.api.runner.*
import org.camunda.community.eze.RecordStream.events
import org.camunda.community.eze.RecordStream.withElementType
import org.camunda.community.eze.RecordStream.withKey
import org.camunda.community.eze.RecordStream.withProcessInstanceKey
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Duration

class EzeTestRunner(
    private val environment: EzeEnvironment = EzeEnvironment(),
    private val beforeEachCallback: (EzeTestContext) -> Unit = {},
    private val afterEachCallback: (EzeTestContext) -> Unit = {}
) : TestRunner {

    private val logger = LoggerFactory.getLogger(EzeTestRunner::class.java)

    private val jobWorkers = mutableListOf<JobWorker>()

    override fun beforeAll() {
        // nothing to do
    }

    override fun beforeEach() {
        logger.debug("Create EZE environment")

        environment.setup()

        beforeEachCallback(EzeTestContext(environment.zeebeClient))
    }

    override fun afterEach() {
        logger.debug("Clean up EZE environment")

        jobWorkers.map(JobWorker::close)

        afterEachCallback(EzeTestContext(environment.zeebeClient))

        environment.cleanUp()
    }

    override fun afterAll() {
        // nothing to do
    }

    override fun deployProcess(name: String, bpmnXml: InputStream) {
        logger.debug("Deploy BPMN process. [name: {}]", name)

        environment.zeebeClient
            .newDeployResourceCommand()
            .addResourceStream(bpmnXml, name)
            .send()
            .join()
    }

    override fun createProcessInstance(
        bpmnProcessId: String,
        variables: String
    ): ProcessInstanceContext {
        logger.debug(
            "Creating a process instance. [BPMN-process-id: {}, variables: {}]",
            bpmnProcessId,
            variables
        )

        val response = environment.zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(bpmnProcessId)
            .latestVersion()
            .variables(variables)
            .send()
            .join()

        return EzeProcessInstanceContext(
            processInstanceKey = response.processInstanceKey
        )
    }

    override fun completeTask(jobType: String, variables: String) {
        logger.debug(
            "Start job worker to complete jobs. [job-type: {}, variables: {}]",
            jobType,
            variables
        )

        val jobWorker = environment.zeebeClient
            .newWorker()
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
        logger.debug(
            "Publish message. [name: {}, correlation-key: {}, variables: {}]",
            messageName, correlationKey, variables
        )

        environment.zeebeClient
            .newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationKey)
            .variables(variables)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }

    override fun throwError(jobType: String, errorCode: String, errorMessage: String) {
        logger.debug(
            "Start job worker to throw errors. [job-type: {}, error-code: {}, error-message: {}]",
            jobType, errorCode, errorMessage
        )

        val jobWorker = environment.zeebeClient
            .newWorker()
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

    override fun cancelProcessInstance(context: ProcessInstanceContext) {
        val processInstanceContext = context as EzeProcessInstanceContext

        logger.debug(
            "Cancel process instance. [key: {}]",
            processInstanceContext.processInstanceKey
        )

        environment.zeebeClient
            .newCancelInstanceCommand(processInstanceContext.processInstanceKey)
            .send()
            .join()
    }

    override fun getProcessInstanceContexts(): List<ProcessInstanceContext> {
        return environment.zeebeEngine
            .processInstanceRecords()
            .withElementType(elementType = BpmnElementType.PROCESS)
            .map { it.value.processInstanceKey }
            .distinct()
            .map { EzeProcessInstanceContext(processInstanceKey = it) }
    }

    override fun getProcessInstanceState(context: ProcessInstanceContext): ProcessInstanceState {
        val processInstanceContext = context as EzeProcessInstanceContext

        val state = environment.zeebeEngine
            .processInstanceRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceContext.processInstanceKey)
            .withElementType(elementType = BpmnElementType.PROCESS)
            .map { it.intent }
            .lastOrNull()

        return when (state) {
            ProcessInstanceIntent.ELEMENT_ACTIVATED -> ProcessInstanceState.ACTIVATED
            ProcessInstanceIntent.ELEMENT_COMPLETED -> ProcessInstanceState.COMPLETED
            ProcessInstanceIntent.ELEMENT_TERMINATED -> ProcessInstanceState.TERMINATED
            else -> ProcessInstanceState.UNKNOWN
        }
    }

    override fun getElementInstances(context: ProcessInstanceContext): List<ElementInstance> {
        val processInstanceContext = context as EzeProcessInstanceContext

        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceContext.processInstanceKey)

        return environment.zeebeEngine
            .processInstanceRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceContext.processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastElementInstanceRecord = records.last()
                val elementId = lastElementInstanceRecord.value.elementId

                ElementInstance(
                    elementId = elementId,
                    elementName = elementNameLookup(elementId),
                    state = when (lastElementInstanceRecord.intent) {
                        ProcessInstanceIntent.ELEMENT_ACTIVATED -> ElementInstanceState.ACTIVATED
                        ProcessInstanceIntent.ELEMENT_COMPLETED -> ElementInstanceState.COMPLETED
                        ProcessInstanceIntent.ELEMENT_TERMINATED -> ElementInstanceState.TERMINATED
                        ProcessInstanceIntent.SEQUENCE_FLOW_TAKEN -> ElementInstanceState.TAKEN
                        else -> ElementInstanceState.UNKNOWN
                    }
                )
            }
    }

    private fun getElementNameLookup(processInstanceKey: Long): (String) -> String? {
        val processDefinitionKey = environment.zeebeEngine
            .processInstanceRecords()
            .events()
            .withProcessInstanceKey(processInstanceKey = processInstanceKey)
            .firstOrNull()
            ?.value
            ?.processDefinitionKey
            ?: -1

        val processRecord = environment.zeebeEngine
            .processRecords()
            .withKey(key = processDefinitionKey)
            .firstOrNull()
            ?.value

        return processRecord
            ?.let {
                val bpmnXml = it.resource
                val bpmnModelInstance = Bpmn.readModelFromStream(ByteArrayInputStream(bpmnXml))

                return { elementId ->
                    if (elementId == it.bpmnProcessId) {
                        it.bpmnProcessId
                    } else {
                        bpmnModelInstance.getModelElementById<FlowElement>(elementId)
                            ?.name
                    }
                }
            }
            ?: { _ -> null }
    }

    override fun getProcessInstanceVariables(context: ProcessInstanceContext): List<ProcessInstanceVariable> {
        val processInstanceContext = context as EzeProcessInstanceContext

        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceContext.processInstanceKey)

        return environment.zeebeEngine
            .variableRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceContext.processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastVariableRecordValue = records.last().value

                val scopeKey = lastVariableRecordValue.scopeKey
                val scopeElementId = getElementId(elementInstanceKey = scopeKey)

                ProcessInstanceVariable(
                    variableName = lastVariableRecordValue.name,
                    variableValue = lastVariableRecordValue.value,
                    scopeElementId = scopeElementId ?: "",
                    scopeElementName = scopeElementId?.let(elementNameLookup)
                )
            }
    }

    private fun getElementId(elementInstanceKey: Long): String? {
        return environment.zeebeEngine
            .processInstanceRecords()
            .withKey(key = elementInstanceKey)
            .firstOrNull()
            ?.value
            ?.elementId
    }

    override fun getIncidents(context: ProcessInstanceContext): List<Incident> {
        val processInstanceContext = context as EzeProcessInstanceContext

        val elementNameLookup =
            getElementNameLookup(processInstanceKey = processInstanceContext.processInstanceKey)

        return environment.zeebeEngine
            .incidentRecords()
            .withProcessInstanceKey(processInstanceKey = processInstanceContext.processInstanceKey)
            .events()
            .groupBy { it.key }
            .map { (_, records) ->
                val lastIncidentRecord = records.last()

                val incidentElementId =
                    getElementId(elementInstanceKey = lastIncidentRecord.value.elementInstanceKey)

                Incident(
                    errorType = lastIncidentRecord.value.errorType.name,
                    errorMessage = lastIncidentRecord.value.errorMessage,
                    state = when (lastIncidentRecord.intent) {
                        IncidentIntent.CREATED -> IncidentState.CREATED
                        IncidentIntent.RESOLVED -> IncidentState.RESOLVED
                        else -> IncidentState.UNKNOWN
                    },
                    elementId = incidentElementId ?: "?",
                    elementName = incidentElementId?.let(elementNameLookup)
                )
            }
    }
}