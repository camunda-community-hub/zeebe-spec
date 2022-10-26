package io.zeebe.bpmnspec.runner.zeebe.eze

import io.camunda.zeebe.client.ZeebeClient
import io.zeebe.bpmnspec.runner.zeebe.TestEnvironment
import io.zeebe.bpmnspec.runner.zeebe.DefaultZeebeEnvironment
import io.zeebe.bpmnspec.runner.zeebe.ZeebeClientZeebeService
import io.zeebe.bpmnspec.runner.zeebe.ZeebeEventRepository
import io.zeebe.bpmnspec.runner.zeebe.ZeebeService
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsZeebeEventRepository
import io.zeebe.hazelcast.exporter.HazelcastExporter
import org.camunda.community.eze.EngineFactory
import org.camunda.community.eze.ZeebeEngine
import org.testcontainers.Testcontainers
import org.testcontainers.containers.wait.strategy.Wait

class DefaultEzeTestEnvironment(
    private val hazelcastExporter: HazelcastExporter = HazelcastExporter(),
    private val zeebeEngine: ZeebeEngine = EngineFactory.create(listOf(hazelcastExporter)),
    private val zeeqsGraphqlPort: Int = 9000,
    private val zeeqsImage: String = "ghcr.io/camunda-community-hub/zeeqs",
    private val zeeqsImageVersion: String = "2.4.0",
    private val zeeqsContainer: DefaultZeebeEnvironment.ZeeqsContainer = DefaultZeebeEnvironment
        .ZeeqsContainer(zeeqsImage, zeeqsImageVersion)
        .withEnv("zeebe.client.worker.hazelcast.connection", "host.testcontainers.internal:5701")
        .withExposedPorts(zeeqsGraphqlPort)
        .waitingFor(Wait.forHttp("/actuator/health")),
    private val zeebeEventRepositoryProvider: (zeeqsClient: ZeeqsClient) -> ZeebeEventRepository = {
        ZeeqsZeebeEventRepository(
            it
        )
    },
    private val zeebeServiceProvider: (zeebeClient: ZeebeClient) -> ZeebeService = { ZeebeClientZeebeService(it) }
) : EzeEnvironment {
    override lateinit var zeebeClient: ZeebeClient
    override lateinit var zeeqsClient: ZeeqsClient
    override lateinit var zeebeEventRepository: ZeebeEventRepository
    override lateinit var zeebeService: ZeebeService
    override var isRunning: Boolean = false
    private val closingSteps = mutableListOf<AutoCloseable>()

    override fun setup() {
        zeebeEngine.start()
        Testcontainers.exposeHostPorts(5701)
        zeebeClient = zeebeEngine.createClient()
        zeebeService = zeebeServiceProvider(zeebeClient)
        zeeqsContainer.start()
        zeeqsClient = ZeeqsClient("localhost:${zeeqsContainer.getMappedPort(zeeqsGraphqlPort)}/graphql")
        zeebeEventRepository = zeebeEventRepositoryProvider(zeeqsClient)
        closingSteps.add(zeebeClient)
        closingSteps.add(zeebeService)
        closingSteps.add(AutoCloseable { zeebeEngine.stop() })
        closingSteps.add(zeeqsContainer)
        isRunning = true
    }

    override fun cleanUp() {
        closingSteps.forEach(AutoCloseable::close)
        isRunning = false
    }
}
