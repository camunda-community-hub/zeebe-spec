package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.time.Duration

class SpecRunnerFactoryProvider : ParameterResolver {

    private var resourceDirectory: String = ""
    private var verificationTimeout: Duration = Duration.ofSeconds(10)
    private var verificationRetryInterval: Duration = Duration.ofMillis(100)

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return parameterContext?.parameter?.type == SpecRunnerFactory::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {

        extensionContext?.element?.map { it.getAnnotation(BpmnSpecRunner::class.java) }?.map {
            resourceDirectory = it.resourceDirectory
            verificationTimeout = it.verificationTimeout.let(Duration::parse)
            verificationRetryInterval = it.verificationRetryInterval.let(Duration::parse)
        }

        val resourceResolver = extensionContext
                ?.testClass
                ?.map {
                    ClasspathResourceResolver(
                            classLoader = it.classLoader,
                            rootDirectory = resourceDirectory)
                }
                ?.orElseThrow()
                ?: throw RuntimeException("ExtensionContext is missing!")

        return SpecRunnerFactory(
                resourceResolver = resourceResolver,
                verificationTimeout = verificationTimeout,
                verificationRetryInterval = verificationRetryInterval)
    }
}