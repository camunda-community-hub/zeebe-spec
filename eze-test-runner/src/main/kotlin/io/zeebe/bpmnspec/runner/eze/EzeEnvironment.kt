package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.client.ZeebeClient
import org.camunda.community.eze.EngineFactory
import org.camunda.community.eze.ZeebeEngine
import org.slf4j.LoggerFactory

class EzeEnvironment(
    val zeebeEngine: ZeebeEngine = EngineFactory.create()
) {

    private val logger = LoggerFactory.getLogger(EzeEnvironment::class.java)

    lateinit var zeebeClient: ZeebeClient

    fun setup() {
        try {
            zeebeEngine.start()

            zeebeClient = zeebeEngine.createClient()

        } catch (e: Exception) {
            // clean up if the creation failed (e.g. free server ports)
            zeebeEngine.stop()

            logger.error("Failed to start the EZE engine", e)
            
            throw RuntimeException("Failed to start the EZE engine", e)
        }

    }

    fun cleanUp() {
        zeebeEngine.stop()
    }

}