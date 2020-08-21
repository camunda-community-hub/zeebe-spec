package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsVerifications
import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.worker.JobWorker
import io.zeebe.containers.ZeebeBrokerContainer
import io.zeebe.containers.ZeebePort
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

class ZeebeEnvironment {

    private val logger = LoggerFactory.getLogger(ZeebeTestRunner::class.java)

    val hazelcastPort = 5701
    val hazelcastHost = "zeebe"
    val zeeqsPort = 9000

    private val closingSteps = mutableListOf<AutoCloseable>()

    lateinit var client: ZeebeClient
    lateinit var zeeqs: ZeeqsVerifications


    class ZeeqsContainer(version: String) : GenericContainer<ZeeqsContainer>("camunda/zeeqs:$version")

    fun setup() {
        val network = Network.newNetwork()
        closingSteps.add(network)

        val zeebeContainer = ZeebeBrokerContainer("camunda/zeebe-with-hazelcast-exporter", "0.24.2-0.10.0-alpha1")
                .withClasspathResourceMapping("application.yaml", "/usr/local/zeebe/config/application.yaml", BindMode.READ_ONLY)
                .withExposedPorts(hazelcastPort)
                .withNetwork(network)
                .withNetworkAliases(hazelcastHost)

        logger.debug("Starting the Zeebe container [image: {}]", zeebeContainer.dockerImageName)
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

        // verify that the client is connected
        client.newTopologyRequest().send().join()

        val zeeqsContainer = ZeeqsContainer("1.0.0-alpha2")
                .withEnv("zeebe.hazelcast.connection", "$hazelcastHost:$hazelcastPort")
                .withExposedPorts(zeeqsPort)
                .dependsOn(zeebeContainer)
                .waitingFor(Wait.forHttp("/actuator/health"))
                .withNetwork(network)
                .withNetworkAliases("zeeqs")

        logger.debug("Starting the ZeeQS container [image: {}]", zeeqsContainer.dockerImageName)
        zeeqsContainer.start()

        logger.debug("Started the ZeeQS container")
        closingSteps.add(zeeqsContainer)

        val zeeqsContainerHost = zeeqsContainer.host
        val zeeqsContainerPort = zeeqsContainer.getMappedPort(zeeqsPort)

        zeeqs = ZeeqsVerifications(zeeqsEndpoint = "$zeeqsContainerHost:$zeeqsContainerPort/graphql")
    }

    fun cleanUp() {
        logger.debug("Closing resources")
        closingSteps.toList().reversed().forEach(AutoCloseable::close)

        logger.debug("Closed resources")
    }

}