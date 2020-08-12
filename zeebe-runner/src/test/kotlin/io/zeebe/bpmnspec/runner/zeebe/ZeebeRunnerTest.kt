package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test

class ZeebeRunnerTest {

    @Test
    fun `should work`() {

        val runner = ZeebeRunner()

        val bpmnXml = ZeebeRunnerTest::class.java.getResourceAsStream("/demo.bpmn")
        runner.deployWorkflow("demo.bpmn", bpmnXml)

        runner.createWorkflowInstance("demo", "{}")

        runner.completeTask("a", "{}")
        runner.completeTask("b", "{}")
        runner.completeTask("c", "{}")

        await.untilAsserted {
            assertThat(runner.getWorkflowInstanceState("demo"))
                    .isEqualTo(WorkflowInstanceState.COMPLETED)
        }

    }

}