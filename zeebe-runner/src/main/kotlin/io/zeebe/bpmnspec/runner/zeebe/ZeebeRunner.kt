package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsVerifications
import io.zeebe.client.ZeebeClient
import io.zeebe.containers.ZeebeBrokerContainer
import io.zeebe.containers.ZeebePort
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

class ZeebeRunner : TestRunner {

    val logger = LoggerFactory.getLogger(ZeebeRunner::class.java)

    val exporterJarPath: Path = Paths.get("../target/exporter/zeebe-hazelcast-exporter.jar")
    val containerPath = "/usr/local/zeebe/exporter/zeebe-hazelcast-exporter.jar"
    val hazelcastPort = 5701
    val hazelcastHost = "zeebe"
    val zeeqsPort = 9000

    val closingSteps = mutableListOf<AutoCloseable>()

    lateinit var client: ZeebeClient
    lateinit var zeeqsVerifications: ZeeqsVerifications

    override fun init() {

        val network = Network.newNetwork()
        closingSteps.add(network)

        val zeebeContainer = ZeebeBrokerContainer("0.24.1")
                .withClasspathResourceMapping("application.yaml", "/usr/local/zeebe/config/application.yaml", BindMode.READ_ONLY)
                .withCopyFileToContainer(MountableFile.forHostPath(exporterJarPath), containerPath)
                .withExposedPorts(hazelcastPort)
                .withNetwork(network)
                .withNetworkAliases(hazelcastHost)

        logger.debug("Starting the Zeebe container")
        zeebeContainer.start()

        logger.debug("Started the Zeebe container")
        closingSteps.add(zeebeContainer)

        val zeebeGatewayPort = zeebeContainer.getExternalAddress(ZeebePort.GATEWAY)

        client = ZeebeClient
                .newClientBuilder()
                .brokerContactPoint(zeebeGatewayPort)
                .usePlaintext()
                .build()

        closingSteps.add(client)

        val topology = client.newTopologyRequest().send().join()
        logger.trace("Zeebe topology: {}", topology)

        val zeeqsContainer = ZeeqsContainer("1.0.0-alpha2")
                .withEnv("zeebe.hazelcast.connection", "$hazelcastHost:$hazelcastPort")
                .withExposedPorts(zeeqsPort)
                .dependsOn(zeebeContainer)
                .waitingFor(Wait.forHttp("/actuator/health"))
                .withNetwork(network)
                .withNetworkAliases("zeeqs")

        logger.debug("Starting the ZeeQS container")
        zeeqsContainer.start()

        logger.debug("Started the ZeeQS container")
        closingSteps.add(zeeqsContainer)

        val zeeqsContainerHost = zeeqsContainer.host
        val zeeqsContainerPort = zeeqsContainer.getMappedPort(zeeqsPort)

        zeeqsVerifications = ZeeqsVerifications(zeeqsEndpoint = "$zeeqsContainerHost:$zeeqsContainerPort/graphql")
    }

    override fun cleanUp() {
        logger.debug("Closing resources")
        closingSteps.toList().reversed().forEach(AutoCloseable::close)

        logger.debug("Closed resources")
    }

    override fun deployWorkflow(name: String, bpmnXml: InputStream) {
        logger.debug("Deploying a BPMN. [name: {}]", name)

        client.newDeployCommand()
                .addResourceStream(bpmnXml, name)
                .send()
                .join()
    }

    override fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext {
        logger.debug("Creating a workflow instance. [BPMN-process-id: {}, variables: {}]", bpmnProcessId, variables)

        val response = client.newCreateInstanceCommand()
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

        client.newWorker()
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

        client.newPublishMessageCommand()
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

        client.newWorker()
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

        client.newCancelInstanceCommand(wfContext.workflowInstanceKey)
                .send()
                .join()
    }

    override fun getWorkflowInstanceContexts(): List<WorkflowInstanceContext> {

        return zeeqsVerifications.getWorkflowInstanceKeys()
                .map { ZeebeWorkflowInstanceContext(workflowInstanceKey = it) }
    }

    override fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState {
        val wfContext = context as ZeebeWorkflowInstanceContext

        val state = zeeqsVerifications.getWorkflowInstanceState(wfContext.workflowInstanceKey)
        return when (state) {
            "COMPLETED" -> WorkflowInstanceState.COMPLETED
            "TERMINATED" -> WorkflowInstanceState.TERMINATED
            "ACTIVATED" -> WorkflowInstanceState.ACTIVATED
            else -> WorkflowInstanceState.UNKNOWN
        }
    }

    override fun getElementInstanceState(context: WorkflowInstanceContext, elementId: String?, elementName: String?): ElementInstanceState {
        val wfContext = context as ZeebeWorkflowInstanceContext

        val state = elementId?.let { zeeqsVerifications.getElementInstanceById(workflowInstanceKey = wfContext.workflowInstanceKey, elementId = it) }
                ?: elementName?.let { zeeqsVerifications.getElementInstanceByName(workflowInstanceKey = wfContext.workflowInstanceKey, elementName = it) }
                ?: "unknown"

        return when (state) {
            "ACTIVATED" -> ElementInstanceState.ACTIVATED
            "COMPLETED" -> ElementInstanceState.COMPLETED
            "TERMINATED" -> ElementInstanceState.TERMINATED
            else -> ElementInstanceState.UNKNOWN
        }
    }

    class ZeeqsContainer(version: String) : GenericContainer<ZeeqsContainer>("camunda/zeeqs:$version")

}