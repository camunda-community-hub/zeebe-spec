package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.format.SpecDeserializer
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.util.stream.Stream


class BpmnSpecArgumentsProvider : ArgumentsProvider, AnnotationConsumer<BpmnSpecSource> {

    private lateinit var specResource: String

    override fun accept(specSource: BpmnSpecSource?) {
        specResource = specSource?.specResource
                ?: throw RuntimeException("The annotation '@BpmnSpecSource' is missing!")
    }

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        context ?: throw RuntimeException("The ExtensionContext is missing!")

        val specDeserializer = SpecDeserializer()

        val resource = context.testClass.map { it.classLoader.getResourceAsStream(specResource) }
                ?.orElseThrow()
                ?: throw RuntimeException("Spec resource not found.")

        val spec = specDeserializer.readSpec(resource)

        return spec.testCases.map {
            BpmnSpecTestCase(
                    resources = spec.resources,
                    testCase = it
            )
        }.map { Arguments.of(it) }.stream()
    }

}