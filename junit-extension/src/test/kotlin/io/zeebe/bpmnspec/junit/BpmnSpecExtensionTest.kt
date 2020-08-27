package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@BpmnSpecRunner
class BpmnSpecExtensionTest(factory: SpecRunnerFactory) {

    private val specRunner = factory.create(testRunner = ZeebeTestRunner())

    @ParameterizedTest
    @BpmnSpecSource(specResource = "exclusive-gateway-spec.yaml")
    fun `exclusive gateway`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

    @ParameterizedTest
    @BpmnSpecSource(specResource = "boundary-event-spec.yaml")
    fun `boundary event`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

}
