package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.runner.eze.EzeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@BpmnSpecRunner
class BpmnSpecExtensionEzeTest(factory: SpecRunnerFactory) {

    private val specRunner = factory.create(testRunner = EzeTestRunner())

    @ParameterizedTest
    @BpmnSpecSource(specResources = ["exclusive-gateway-spec.yaml", "boundary-event-spec.yaml"])
    fun `should pass the BPMN spec`(spec: BpmnSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
            .describedAs(testResult.message)
            .isTrue()
    }

}
