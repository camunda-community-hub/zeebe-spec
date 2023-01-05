package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.client.ZeebeClient
import io.zeebe.bpmnspec.api.SpecTestRunnerContext
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider
import io.zeebe.bpmnspec.runner.TestRunnerEnvironment
import io.zeebe.bpmnspec.runner.zeebe.ZeebeSpecActionExecutor
import org.camunda.community.eze.EngineFactory
import org.camunda.community.eze.ZeebeEngine
import org.slf4j.LoggerFactory

class EzeEnvironment(
    private val zeebeEngine: ZeebeEngine = EngineFactory.create()
) : TestRunnerEnvironment {

    private val logger = LoggerFactory.getLogger(EzeEnvironment::class.java)

    override val actionExecutor: SpecActionExecutor =
        ZeebeSpecActionExecutor(zeebeClientProvider = zeebeEngine::createClient)

    override val stateProvider: SpecStateProvider =
        EzeSpecStateProvider(zeebeEngineProvider = this::zeebeEngine)

    override fun create() {
        logger.debug("Start EZE engine")

        try {
            zeebeEngine.start()

        } catch (e: Exception) {
            // clean up if the creation failed (e.g. free server ports)
            zeebeEngine.stop()

            logger.error("Failed to start the EZE engine", e)

            throw RuntimeException("Failed to start the EZE engine", e)
        }
    }

    override fun getContext(): SpecTestRunnerContext {
        return EzeContext(zeebeClient = zeebeEngine.createClient())
    }

    override fun close() {
        logger.debug("Close EZE engine")

        actionExecutor.close()
        stateProvider.close()

        zeebeEngine.stop()
    }

    private data class EzeContext(private val zeebeClient: ZeebeClient) : SpecTestRunnerContext {

        override fun getZeebeClient(): ZeebeClient = zeebeClient

    }
}