package org.camunda.community.zeebe.spec.junit

import io.camunda.zeebe.client.ZeebeClient
import org.camunda.community.zeebe.spec.api.SpecTestRunnerContext
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.ExceptionUtils
import org.junit.platform.commons.util.ReflectionUtils
import java.lang.reflect.Field
import kotlin.reflect.KClass

class SpecRunnerInjectCallback : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        context
            ?.requiredTestInstances
            ?.allInstances
            ?.forEach {
                injectFields(context, it, it.javaClass)
            }
    }

    private fun injectFields(context: ExtensionContext, testInstance: Any, testClass: Class<*>) {

        val specTestRunnerContext = context
            .getStore(SpecRunnerProvider.extensionContextNamespace)
            .get(SpecRunnerProvider.extensionContextStoreKey, SpecTestRunnerContext::class.java)
            ?: throw RuntimeException("Expect spec test runner context but not found")

        injectFields(
            specTestRunnerContext,
            testInstance,
            testClass,
            ZeebeClient::class
        ) { it.getZeebeClient() }
    }

    private fun injectFields(
        specTestRunnerContext: SpecTestRunnerContext,
        testInstance: Any,
        testClass: Class<*>,
        fieldType: KClass<*>,
        fieldValue: (SpecTestRunnerContext) -> Any
    ) {
        val fields = ReflectionUtils.findFields(
            testClass,
            { field: Field -> ReflectionUtils.isNotStatic(field) && field.type == fieldType.java },
            ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
        )

        fields.forEach { field: Field ->
            try {
                ReflectionUtils.makeAccessible(field)[testInstance] =
                    fieldValue(specTestRunnerContext)
            } catch (t: Throwable) {
                ExceptionUtils.throwAsUncheckedException(t)
            }
        }
    }

}