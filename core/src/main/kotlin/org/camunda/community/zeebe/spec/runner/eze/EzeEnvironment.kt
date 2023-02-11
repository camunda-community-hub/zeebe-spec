package org.camunda.community.zeebe.spec.runner.eze

import io.camunda.zeebe.client.ZeebeClient
import org.camunda.community.zeebe.spec.api.SpecTestRunnerContext
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.camunda.community.zeebe.spec.runner.SpecStateProvider
import org.camunda.community.zeebe.spec.runner.TestRunnerEnvironment
import org.camunda.community.zeebe.spec.runner.zeebe.ZeebeSpecActionExecutor
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