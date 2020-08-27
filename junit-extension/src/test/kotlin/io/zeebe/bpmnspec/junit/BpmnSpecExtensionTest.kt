package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@BpmnSpecRunner
class BpmnSpecExtensionTest(factory: SpecRunnerFactory) {

    private val specRunner = factory.create(testRunner = ZeebeTestRunner())

    @ParameterizedTest
    @BpmnSpecSource(specResources = ["exclusive-gateway-spec.yaml", "boundary-event-spec.yaml"])
    fun `should pass the BPMN spec`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

}
