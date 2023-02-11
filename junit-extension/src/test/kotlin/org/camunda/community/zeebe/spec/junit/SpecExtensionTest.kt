package org.camunda.community.zeebe.spec.junit

import org.camunda.community.zeebe.spec.SpecRunner
import org.camunda.community.zeebe.spec.assertj.SpecAssertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@ZeebeSpecRunner
class SpecExtensionTest(private val specRunner: SpecRunner) {

    @ParameterizedTest
    @ZeebeSpecSource(specResources = ["exclusive-gateway-spec.yaml", "boundary-event-spec.yaml"])
    fun `should pass the BPMN spec`(spec: ZeebeSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(testcase = spec.testCase)

        assertThat(testResult).isSuccessful()
    }

    @ParameterizedTest
    @ZeebeSpecSource(specDirectory = "specs")
    fun `should run all specs in directory`(spec: ZeebeSpecTestCase) {

        val testResult =
            specRunner.runSingleTestCase(testcase = spec.testCase)

        assertThat(testResult).isSuccessful()
    }

}
