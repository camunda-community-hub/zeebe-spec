package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.format.SpecDeserializer
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.util.stream.Stream


class BpmnSpecTestCaseArgumentsProvider : ArgumentsProvider, AnnotationConsumer<BpmnSpecSource> {

    private val specDeserializer = SpecDeserializer()

    private val specResources = mutableListOf<String>()

    override fun accept(specSource: BpmnSpecSource?) {
        specSource?.specResources?.forEach { specResources.add(it) }
                ?: throw RuntimeException("annotation @BpmnSpecSource no found")
    }

    override fun provideArguments(extensionContext: ExtensionContext?): Stream<out Arguments> {

        if (specResources.isEmpty()) {
            throw RuntimeException("no spec resources defined")
        }

        return specResources
                .map { specResource ->
                    extensionContext
                            ?.testClass
                            ?.map { it.classLoader.getResourceAsStream(specResource) }
                            ?.orElseThrow { RuntimeException("no spec resource found with name '$specResource' in classpath") }
                            ?: throw RuntimeException("extension context not found")
                }
                .map { resource -> specDeserializer.readSpec(resource) }
                .flatMap { spec ->
                    spec.testCases.map {
                        BpmnSpecTestCase(
                                resources = spec.resources,
                                testCase = it
                        )
                    }
                }
                .map { Arguments.of(it) }
                .stream()
    }

}