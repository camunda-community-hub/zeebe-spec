package io.zeebe.bpmnspec.junit

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(SpecRunnerFactoryProvider::class)
annotation class BpmnSpecRunner(
        val resourceDirectory: String = ""
)