package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest

@BpmnSpecRunner
class BpmnSpecExtensionReuseEnvironmentTest {

    @Disabled("blocked by the bug #82")
    @ParameterizedTest
    @BpmnSpecSource(specResources = ["spec-with-process-alias.yaml"])
    fun `exclusive gateway`(spec: BpmnSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
            .describedAs(testResult.message)
            .isTrue()
    }

    companion object {

        private val testRunner = ZeebeTestRunner(
            reuseEnvironment = true
        )

        lateinit var specRunner: SpecRunner

        @BeforeAll
        @JvmStatic
        internal fun beforeAll(factory: SpecRunnerFactory) {
            specRunner = factory.create(testRunner = testRunner)

            testRunner.beforeAll()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            testRunner.afterAll()
        }
    }

}
