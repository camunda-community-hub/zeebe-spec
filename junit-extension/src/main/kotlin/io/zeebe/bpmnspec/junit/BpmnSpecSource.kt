package io.zeebe.bpmnspec.junit

import org.junit.jupiter.params.provider.ArgumentsSource

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(BpmnSpecArgumentsProvider::class)
annotation class BpmnSpecSource(
        val specResource: String
)