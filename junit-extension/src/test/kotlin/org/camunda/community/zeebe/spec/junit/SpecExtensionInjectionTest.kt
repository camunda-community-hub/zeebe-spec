package org.camunda.community.zeebe.spec.junit

import io.camunda.zeebe.client.ZeebeClient
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.zeebe.spec.SpecRunner
import org.camunda.community.zeebe.spec.assertj.SpecAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest

@ZeebeSpecRunner(verificationTimeout = "PT1S")
class SpecExtensionInjectionTest(private val specRunner: SpecRunner) {

    private lateinit var zeebeClient: ZeebeClient

    @BeforeEach
    fun `start external worker`() {
        zeebeClient.newWorker()
            .jobType("external-worker")
            .handler { client, job ->
                val valueOfX = job.variablesAsMap["x"] as Int
                val newValue = valueOfX + 1

                client.newCompleteCommand(job.key)
                    .variables(mapOf("x" to newValue))
                    .send()
                    .join()
            }
            .open()
    }

    @ParameterizedTest
    @ZeebeSpecSource(specResources = ["spec-with-external-worker.yaml"])
    fun `should inject Zeebe client and complete process`(spec: ZeebeSpecTestCase) {

        assertThat(zeebeClient)
            .describedAs("Zeebe client should be injected")
            .isNotNull()

        val testResult =
            specRunner.runSingleTestCase(testcase = spec.testCase)

        SpecAssertions.assertThat(testResult).isSuccessful()
    }

}
