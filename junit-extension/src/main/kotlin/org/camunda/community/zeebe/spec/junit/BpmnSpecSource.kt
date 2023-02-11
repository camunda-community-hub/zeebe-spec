package org.camunda.community.zeebe.spec.junit

import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * The spec source must define either a list of spec resources of a directory that contains the spec
 * resources. The spec resources are loaded from the classpath.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(BpmnSpecTestCaseArgumentsProvider::class)
annotation class BpmnSpecSource(
    val specResources: Array<String> = [],
    val specDirectory: String = ""
)