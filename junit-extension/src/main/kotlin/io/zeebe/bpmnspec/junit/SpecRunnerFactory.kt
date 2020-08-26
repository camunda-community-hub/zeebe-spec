package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.TestRunner
import java.time.Duration

class SpecRunnerFactory(private val resourceResolver: ResourceResolver,
                        private val verificationTimeout: Duration,
                        private val verificationRetryInterval: Duration) {

    fun create(testRunner: TestRunner): SpecRunner = SpecRunner(
            testRunner = testRunner,
            resourceResolver = resourceResolver,
            verificationTimeout = verificationTimeout,
            verificationRetryInterval = verificationRetryInterval
    )

}