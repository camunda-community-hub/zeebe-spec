package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.runner.zeebe.ZeebeRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path

class BpmnSpecExtensionTest {

    @TestTemplate
    @ExtendWith(BpmnSpecContextProvider::class)
    @BpmnSpec(specResource = "demo.yaml")
    fun `run demo`(spec: BpmnSpecTestCase) {

        val resource = BpmnSpecExtensionTest::class.java.getResource("/demo.yaml")
        val classpathDir = Path.of(resource.toURI()).parent

        val runner = SpecRunner(
                testRunner = ZeebeRunner(),
                resourceDirectory = classpathDir)

        val testResult = runner.runTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

}