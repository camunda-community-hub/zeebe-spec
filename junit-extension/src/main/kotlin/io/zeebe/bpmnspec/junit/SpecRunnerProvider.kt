package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.time.Duration

class SpecRunnerProvider : ParameterResolver {

    private val defaultResourceDirectory = ""
    private val defaultVerificationTimeout = Duration.ofSeconds(10)
    private val defaultVerificationRetryInterval = Duration.ofMillis(100)

    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Boolean {
        return parameterContext?.parameter?.type == SpecRunner::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Any {

        val specRunnerAnnotation = extensionContext
            ?.element
            ?.map { it.getAnnotation(BpmnSpecRunner::class.java) }

        val resourceDirectory: String = specRunnerAnnotation
            ?.map { it.resourceDirectory }
            ?.orElse(defaultResourceDirectory)
            ?: defaultResourceDirectory

        val resourceResolver = extensionContext
            ?.testClass
            ?.map {
                ClasspathResourceResolver(
                    classLoader = it.classLoader,
                    rootDirectory = resourceDirectory
                )
            }
            ?.orElseThrow { RuntimeException("no test class found") }
            ?: throw RuntimeException("no extension context found")

        val verificationTimeout = specRunnerAnnotation
            ?.map { it.verificationTimeout }
            ?.map { Duration.parse(it) }
            ?.orElse(defaultVerificationTimeout)
            ?: defaultVerificationTimeout

        val verificationRetryInterval = specRunnerAnnotation
            ?.map { it.verificationRetryInterval }
            ?.map { Duration.parse(it) }
            ?.orElse(defaultVerificationRetryInterval)
            ?: defaultVerificationRetryInterval

        return SpecRunner(
            resourceResolver = resourceResolver,
            verificationTimeout = verificationTimeout,
            verificationRetryInterval = verificationRetryInterval
        )
    }
}