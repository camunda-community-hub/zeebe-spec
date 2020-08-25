package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.TestRunner

class SpecRunnerFactory(private val resourceResolver: ResourceResolver) {

    fun create(testRunner: TestRunner): SpecRunner = SpecRunner(
            testRunner = testRunner,
            resourceResolver = resourceResolver
    )

}