package org.camunda.community.zeebe.spec.junit

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(SpecRunnerProvider::class, SpecRunnerInjectCallback::class)
annotation class ZeebeSpecRunner(
    val resourceDirectory: String = "",
    val verificationTimeout: String = "PT10S",
    val verificationRetryInterval: String = "PT0.1S"
)