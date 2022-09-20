package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.runner.zeebe.eze.EzeTestEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class ZeebeTestRunnerTest {

    @ParameterizedTest
    @ArgumentsSource(TestEnvironmentArgumentsProvider::class)
    fun `ZeebeRunner should work standalone`(testEnvironment: TestEnvironment) {

        val runner = ZeebeTestRunner(testEnvironment)

        runner.beforeEach()

        val bpmnXml = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.bpmn")
        runner.deployProcess("demo.bpmn", bpmnXml)

        val wfContext = runner.createProcessInstance("demo", "{}")

        runner.completeTask("a", "{}")
        runner.completeTask("b", "{}")
        runner.completeTask("c", "{}")

        await.untilAsserted {
            assertThat(runner.getProcessInstanceState(wfContext))
                .isEqualTo(ProcessInstanceState.COMPLETED)
        }

        runner.afterEach()
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `Runner with ZeebeTestRunner should run the YAML spec`(specRunner: SpecRunner) {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `Runner with ZeebeTestRunner should run the Kotlin spec`(specRunner: SpecRunner) {

        val spec = DemoTestSpecBuilder.demo()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should run the YAML spec with message`(specRunner: SpecRunner) {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo3.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should run the Kotlin spec with message`(specRunner: SpecRunner) {

        val spec = DemoTestSpecBuilder.demo3()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }


    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should run the YAML spec with incident`(specRunner: SpecRunner) {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo-incident.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should run the Kotlin spec with incident`(specRunner: SpecRunner) {

        val spec = DemoTestSpecBuilder.demoIncident()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }


    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should fail verification`(specRunner: SpecRunner) {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/failed-test-case.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        assertThat(testResult.success).isFalse()
        assertThat(testResult.message).isEqualTo("Expected the element with name 'B'  to be in state 'COMPLETED' but was 'ACTIVATED'.")

        assertThat(testResult.testCase.verifications).hasSize(3)
        assertThat(testResult.successfulVerifications).containsExactly(testResult.testCase.verifications[0])
        assertThat(testResult.failedVerification).isEqualTo(testResult.testCase.verifications[1])
    }

    @ParameterizedTest
    @ArgumentsSource(SpecRunnerWithDifferentTestEnvironmentArgumentsProvider::class)
    fun `should collect output`(specRunner: SpecRunner) {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        assertThat(testResult.success).isTrue()
        assertThat(testResult.output).hasSize(1)

        val testOutput = testResult.output[0];
        assertThat(testOutput.state).isEqualTo(ProcessInstanceState.COMPLETED)
        assertThat(testOutput.elementInstances).hasSize(10)
        assertThat(testOutput.variables).isEmpty()
        assertThat(testOutput.incidents).isEmpty()
    }

    class SpecRunnerWithDifferentTestEnvironmentArgumentsProvider : ArgumentsProvider {

        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            val resourceResolver = ClasspathResourceResolver(classLoader = ZeebeTestRunnerTest::class.java.classLoader)
            return Stream.of(
                Arguments.of(
                    SpecRunner(
                        testRunner = ZeebeTestRunner(),
                        resourceResolver = resourceResolver
                    )
                ),
                Arguments.of(
                    SpecRunner(
                        testRunner = ZeebeTestRunner(EzeTestEnvironment()),
                        resourceResolver = resourceResolver
                    )
                )
            )
        }
    }

    class TestEnvironmentArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = Stream.of(
            Arguments.of(EzeTestEnvironment()),
            Arguments.of(ZeebeEnvironment())
        )

    }
}