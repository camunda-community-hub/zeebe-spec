package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.SpecTestRunnerContext
import io.zeebe.bpmnspec.runner.TestRunnerEnvironment
import io.zeebe.bpmnspec.runner.eze.EzeEnvironment
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
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

        val environment: TestRunnerEnvironment = EzeEnvironment()

        // the context is used for injecting the Zeebe client
        storeTestRunnerContext(extensionContext, environment.getContext())

        return SpecRunner(
            resourceResolver = resourceResolver,
            environment = environment,
            verificationTimeout = verificationTimeout,
            verificationRetryInterval = verificationRetryInterval
        )
    }

    private fun storeTestRunnerContext(
        extensionContext: ExtensionContext?,
        specTestRunnerContext: SpecTestRunnerContext
    ) {
        extensionContext
            ?.getStore(extensionContextNamespace)
            ?.put(extensionContextStoreKey, specTestRunnerContext)
    }

    companion object {
        val extensionContextNamespace: Namespace = Namespace.create(SpecRunnerProvider::class.java)

        const val extensionContextStoreKey = "spec-runner-environment"
    }

}
