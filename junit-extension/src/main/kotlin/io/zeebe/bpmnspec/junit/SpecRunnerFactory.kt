package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.TestRunner
import java.time.Duration

class SpecRunnerFactory(private val resourceResolver: ResourceResolver) {

    fun create(testRunner: TestRunner,
               verificationTimeout: Duration = Duration.ofSeconds(10),
               verificationRetryInterval: Duration = Duration.ofMillis(100)
    ): SpecRunner =
            SpecRunner(
                    testRunner = testRunner,
                    resourceResolver = resourceResolver,
                    verificationTimeout = verificationTimeout,
                    verificationRetryInterval = verificationRetryInterval
            )

}