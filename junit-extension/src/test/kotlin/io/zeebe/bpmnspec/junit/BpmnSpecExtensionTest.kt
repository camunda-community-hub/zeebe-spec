package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

class BpmnSpecExtensionTest {

    private val specRunner = SpecRunner(
            testRunner = ZeebeTestRunner(),
            resourceResolver = ClasspathResourceResolver(BpmnSpecExtensionTest::class.java.classLoader))

    @TestTemplate
    @ExtendWith(BpmnSpecContextProvider::class)
    @BpmnSpec(specResource = "exclusive-gateway-spec.yaml")
    fun `exclusive gateway`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

    @TestTemplate
    @ExtendWith(BpmnSpecContextProvider::class)
    @BpmnSpec(specResource = "boundary-event-spec.yaml")
    fun `boundary event`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

}