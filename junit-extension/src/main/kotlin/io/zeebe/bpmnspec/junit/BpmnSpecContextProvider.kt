package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.format.SpecDeserializer
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import java.util.stream.Stream

class BpmnSpecContextProvider : TestTemplateInvocationContextProvider {

    override fun supportsTestTemplate(context: ExtensionContext?): Boolean {
        return true
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext?): Stream<TestTemplateInvocationContext> {

        val specResource = context?.element?.map { it.getAnnotation(BpmnSpec::class.java) }?.map { it.specResource }
                ?.orElseThrow { RuntimeException("The annotation '@BpmnSpec' is missing!") }
                ?: throw RuntimeException("The annotation '@BpmnSpec' is missing!")

        val specDeserializer = SpecDeserializer()

        val resource = context.testClass.map { it.classLoader.getResourceAsStream(specResource) }
                ?.orElseThrow()
                ?: throw RuntimeException("Spec resource not found.")

        val spec = specDeserializer.readSpec(resource)

        return spec.testCases.map {
            BpmnSpecInvocationContext(displayName = it.name, testCase = BpmnSpecTestCase(
                    resources = spec.resources,
                    testCase = it
            ))
        }.map { it as TestTemplateInvocationContext }.stream()
    }

    class BpmnSpecInvocationContext(val displayName: String, val testCase: BpmnSpecTestCase) : TestTemplateInvocationContext {
        override fun getDisplayName(invocationIndex: Int): String {
            return displayName
        }

        override fun getAdditionalExtensions(): MutableList<Extension> {
            return mutableListOf(
                    BpmnSpecResolver(testCase)
            )
        }
    }
}
