package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class SpecRunnerFactoryProvider: ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return parameterContext?.parameter?.type == SpecRunnerFactory::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {

        val resourceResolver =extensionContext?.testClass?.map { ClasspathResourceResolver(classLoader = it.classLoader) }
                ?.orElseThrow()
                ?: throw RuntimeException("ExtensionContext is missing!")

        return SpecRunnerFactory(resourceResolver = resourceResolver)
    }
}