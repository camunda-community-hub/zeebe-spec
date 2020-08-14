package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ZeebeRunnerTest {

    @Test
    fun `ZeebeRunner should work standalone`() {

        val runner = ZeebeRunner()

        runner.init()

        val bpmnXml = ZeebeRunnerTest::class.java.getResourceAsStream("/demo.bpmn")
        runner.deployWorkflow("demo.bpmn", bpmnXml)

        val wfContext = runner.createWorkflowInstance("demo", "{}")

        runner.completeTask("a", "{}")
        runner.completeTask("b", "{}")
        runner.completeTask("c", "{}")

        await.untilAsserted {
            assertThat(runner.getWorkflowInstanceState(wfContext))
                    .isEqualTo(WorkflowInstanceState.COMPLETED)
        }

        runner.cleanUp()
    }

    @Test
    fun `Runner with ZeebeTestRunner should run the spec`() {

        val classpathDir = ZeebeRunnerTest::class.java.getResource("/demo.yaml")
        val classpath = Path.of(classpathDir.toURI()).parent

        val runner = SpecRunner(
                testRunner = ZeebeRunner(),
                resourceDirectory = classpath)

        val spec = ZeebeRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = runner.run(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run the spec with message`() {

        val classpathDir = ZeebeRunnerTest::class.java.getResource("/demo3.yaml")
        val classpath = Path.of(classpathDir.toURI()).parent

        val runner = SpecRunner(
                testRunner = ZeebeRunner(),
                resourceDirectory = classpath)

        val spec = ZeebeRunnerTest::class.java.getResourceAsStream("/demo3.yaml")
        val result = runner.run(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run the spec with incident`() {

        val classpathDir = ZeebeRunnerTest::class.java.getResource("/demo-incident.yaml")
        val classpath = Path.of(classpathDir.toURI()).parent

        val runner = SpecRunner(
                testRunner = ZeebeRunner(),
                resourceDirectory = classpath)

        val spec = ZeebeRunnerTest::class.java.getResourceAsStream("/demo-incident.yaml")
        val result = runner.run(spec)

        assertThat(result.testResults).hasSize(1)
    }

}