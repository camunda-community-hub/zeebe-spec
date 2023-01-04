package io.zeebe.bpmnspec.runner.eze

import io.camunda.zeebe.client.ZeebeClient
import org.camunda.community.eze.EngineFactory
import org.camunda.community.eze.ZeebeEngine

class EzeEnvironment(
    val zeebeEngine: ZeebeEngine = EngineFactory.create()
) {

    lateinit var zeebeClient: ZeebeClient

    fun setup() {
        zeebeEngine.start()

        zeebeClient = zeebeEngine.createClient()
    }

    fun cleanUp() {
        zeebeEngine.stop()
    }

}