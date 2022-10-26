package io.zeebe.bpmnspec.runner.zeebe.eze

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunnerTest
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test

internal class EzeTestRunnerTest {

    private val resourceResolver =
        ClasspathResourceResolver(classLoader = EzeTestRunnerTest::class.java.classLoader)
    private val specRunner = SpecRunner(
        testRunner = EzeTestRunner(),
        resourceResolver = resourceResolver
    )

    @Test
    internal fun `EzeRunner should work standalone`() {

        val runner = EzeTestRunner()

        runner.beforeEach()

        val bpmnXml = EzeTestRunnerTest::class.java.getResourceAsStream("/demo.bpmn")
        runner.deployProcess("demo.bpmn", bpmnXml)

        val wfContext = runner.createProcessInstance("demo", "{}")

        runner.completeTask("a", "{}")
        runner.completeTask("b", "{}")
        runner.completeTask("c", "{}")

        await.untilAsserted {
            Assertions.assertThat(runner.getProcessInstanceState(wfContext))
                .isEqualTo(ProcessInstanceState.COMPLETED)
        }

        runner.afterEach()
    }

    @Test
    internal fun `Runner with EzeTestRunner should run the YAML spec`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)
    }
}
