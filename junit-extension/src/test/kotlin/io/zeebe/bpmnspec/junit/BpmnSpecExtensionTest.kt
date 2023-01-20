package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.assertj.SpecAssertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@BpmnSpecRunner
class BpmnSpecExtensionTest(private val specRunner: SpecRunner) {

    @ParameterizedTest
    @BpmnSpecSource(specResources = ["exclusive-gateway-spec.yaml", "boundary-event-spec.yaml"])
    fun `should pass the BPMN spec`(spec: BpmnSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult).isSuccessful()
    }

    @ParameterizedTest
    @BpmnSpecSource(specDirectory = "specs")
    fun `should run all specs in directory`(spec: BpmnSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult).isSuccessful()
    }

}
