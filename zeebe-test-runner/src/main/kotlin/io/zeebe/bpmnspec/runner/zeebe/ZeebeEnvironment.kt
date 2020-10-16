package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient
import io.zeebe.client.ZeebeClient
import io.zeebe.containers.ZeebeBrokerContainer
import io.zeebe.containers.ZeebePort
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

class ZeebeEnvironment (
        val zeebeImage : String = System.getProperty("zeebeImage", "camunda/zeebe-with-hazelcast-exporter"),
        val zeebeImageVersion : String = System.getProperty("zeebeImageVersion", "0.24.2-0.10.0-alpha1")) {

    private val logger = LoggerFactory.getLogger(ZeebeTestRunner::class.java)

    val zeeqsImage = "camunda/zeeqs"
    val zeeqsImageVersion: String = "1.0.0-alpha2"

    private val zeebeHost = "zeebe"
    private val hazelcastPort = 5701
    private val zeeqsGraphqlPort = 9000

    private val closingSteps = mutableListOf<AutoCloseable>()

    lateinit var zeebeClient: ZeebeClient
    lateinit var zeeqsClient: ZeeqsClient

    var isRunning = false

    fun setup() {
        val network = Network.newNetwork()
        closingSteps.add(network)

        val zeebeContainer = ZeebeBrokerContainer(zeebeImage, zeebeImageVersion)
                .withClasspathResourceMapping("application.yaml", "/usr/local/zeebe/config/application.yaml", BindMode.READ_ONLY)
                .withExposedPorts(hazelcastPort)
                .withNetwork(network)
                .withNetworkAliases(zeebeHost)

        logger.debug("Starting the Zeebe container [image: {}]", zeebeContainer.dockerImageName)
        try {
            zeebeContainer.start()
        } catch (e: Exception) {
            logger.error("Failed to start the Zeebe container", e)
            logger.debug("Zeebe container output: {}", zeebeContainer.logs)

            throw RuntimeException("Failed to start the Zeebe container", e)
        }

        logger.debug("Started the Zeebe container")
        closingSteps.add(zeebeContainer)

        val zeebeGatewayPort = zeebeContainer.getExternalAddress(ZeebePort.GATEWAY)

        zeebeClient = ZeebeClient
                .newClientBuilder()
                .brokerContactPoint(zeebeGatewayPort)
                .usePlaintext()
                .build()

        closingSteps.add(zeebeClient)

        // verify that the client is connected
        try {
            zeebeClient.newTopologyRequest().send().join()
        } catch (e: Exception) {
            logger.error("Failed to connect the Zeebe client", e)
            logger.debug("Zeebe container output: {}", zeebeContainer.logs)

            throw RuntimeException("Failed to connect the Zeebe client", e)
        }

        val zeeqsContainer = ZeeqsContainer(zeeqsImage, zeeqsImageVersion)
                .withEnv("zeebe.hazelcast.connection", "$zeebeHost:$hazelcastPort")
                .withExposedPorts(zeeqsGraphqlPort)
                .dependsOn(zeebeContainer)
                .waitingFor(Wait.forHttp("/actuator/health"))
                .withNetwork(network)
                .withNetworkAliases("zeeqs")

        logger.debug("Starting the ZeeQS container [image: {}]", zeeqsContainer.dockerImageName)
        try {
            zeeqsContainer.start()
        } catch (e: Exception) {
            logger.error("Failed to start the ZeeQS container", e)
            logger.debug("Zeebe container output: {}", zeebeContainer.logs)
            logger.debug("ZeeQS container output: {}", zeeqsContainer.logs)

            throw RuntimeException("Failed to start the ZeeQS container", e)
        }

        logger.debug("Started the ZeeQS container")
        closingSteps.add(zeeqsContainer)

        val zeeqsContainerHost = zeeqsContainer.host
        val zeeqsContainerPort = zeeqsContainer.getMappedPort(zeeqsGraphqlPort)

        zeeqsClient = ZeeqsClient(zeeqsEndpoint = "$zeeqsContainerHost:$zeeqsContainerPort/graphql")

        isRunning = true
    }

    fun cleanUp() {
        logger.debug("Closing resources")
        closingSteps.toList().reversed().forEach(AutoCloseable::close)

        logger.debug("Closed resources")

        isRunning = false
    }

    class ZeeqsContainer(imageName: String, version: String) : GenericContainer<ZeeqsContainer>("$imageName:$version")

}