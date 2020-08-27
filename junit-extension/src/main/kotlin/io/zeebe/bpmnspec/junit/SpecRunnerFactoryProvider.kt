package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class SpecRunnerFactoryProvider : ParameterResolver {

    private val defaultResourceDirectory = ""

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return parameterContext?.parameter?.type == SpecRunnerFactory::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {

        val resourceDirectory: String = extensionContext
                ?.element
                ?.map { it.getAnnotation(BpmnSpecRunner::class.java) }
                ?.map { it.resourceDirectory }
                ?.orElse(defaultResourceDirectory)
                ?: defaultResourceDirectory

        val resourceResolver = extensionContext
                ?.testClass
                ?.map {
                    ClasspathResourceResolver(
                            classLoader = it.classLoader,
                            rootDirectory = resourceDirectory)
                }
                ?.orElseThrow { RuntimeException("no test class found") }
                ?: throw RuntimeException("no extension context found")

        return SpecRunnerFactory(resourceResolver = resourceResolver)
    }
}