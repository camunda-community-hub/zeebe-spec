import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.runner.eze.EzeTestRunner
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class EzeTestRunnerTest {

    private val resourceResolver =
        ClasspathResourceResolver(classLoader = EzeTestRunnerTest::class.java.classLoader)

    private val specRunner = SpecRunner(
        testRunner = EzeTestRunner(),
        resourceResolver = resourceResolver,
        verificationTimeout = Duration.ofSeconds(3)
    )

    @Test
    fun `should run a YAML spec`() {

        val spec = EzeTestRunnerTest::class.java.getResourceAsStream("/demo-complete-process.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run a YAML spec and publish a message`() {

        val spec = EzeTestRunnerTest::class.java.getResourceAsStream("/demo-publish-message.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run a YAML spec and verify incident`() {

        val spec = EzeTestRunnerTest::class.java.getResourceAsStream("/demo-incident.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should fail verification`() {

        val spec = EzeTestRunnerTest::class.java.getResourceAsStream("/demo-fail-verification.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        Assertions.assertThat(testResult.success).isFalse()
        Assertions.assertThat(testResult.message)
            .isEqualTo("Expected the element with name 'B'  to be in state 'COMPLETED' but was 'ACTIVATED'.")

        Assertions.assertThat(testResult.testCase.verifications).hasSize(3)
        Assertions.assertThat(testResult.successfulVerifications)
            .containsExactly(testResult.testCase.verifications[0])
        Assertions.assertThat(testResult.failedVerification)
            .isEqualTo(testResult.testCase.verifications[1])
    }

    @Test
    fun `should collect output`() {

        val spec = EzeTestRunnerTest::class.java.getResourceAsStream("/demo-complete-process.yaml")
        val result = specRunner.runSpec(spec)

        Assertions.assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        Assertions.assertThat(testResult.success).isTrue()
        Assertions.assertThat(testResult.output).hasSize(1)

        val testOutput = testResult.output[0];
        Assertions.assertThat(testOutput.state).isEqualTo(ProcessInstanceState.COMPLETED)
        Assertions.assertThat(testOutput.elementInstances).hasSize(10)
        Assertions.assertThat(testOutput.variables).isEmpty()
        Assertions.assertThat(testOutput.incidents).isEmpty()
    }

}