package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.format.SpecDeserializer
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.util.stream.Stream


class BpmnSpecTestCaseArgumentsProvider : ArgumentsProvider, AnnotationConsumer<BpmnSpecSource> {

    private val specDeserializer = SpecDeserializer()

    private lateinit var specResource: String

    override fun accept(specSource: BpmnSpecSource?) {
        specResource = specSource?.specResource
                ?: throw RuntimeException("annotation @BpmnSpecSource no found")
    }

    override fun provideArguments(extensionContext: ExtensionContext?): Stream<out Arguments> {

        val resource = extensionContext
                ?.testClass
                ?.map { it.classLoader.getResourceAsStream(specResource) }
                ?.orElseThrow { RuntimeException("no spec resource found with name '$specResource' in classpath") }
                ?: throw RuntimeException("extension context not found")

        val spec = specDeserializer.readSpec(resource)

        return spec.testCases.map {
            BpmnSpecTestCase(
                    resources = spec.resources,
                    testCase = it
            )
        }.map { Arguments.of(it) }.stream()
    }

}