package org.camunda.community.zeebe.spec.junit

import org.camunda.community.zeebe.spec.format.SpecDeserializer
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.isDirectory


class ZeebeSpecTestCaseArgumentsProvider : ArgumentsProvider, AnnotationConsumer<ZeebeSpecSource> {

    private val specDeserializer = SpecDeserializer()

    private val specResources = mutableListOf<String>()
    private var specDirectory: String? = null

    override fun accept(specSource: ZeebeSpecSource?) {
        specSource?.let { source ->
            source.specResources.forEach { resource -> specResources.add(resource) }
            source.specDirectory.takeIf { it.isNotEmpty() }.let { specDirectory = it }
        }
            ?: throw RuntimeException("annotation @ZeebeSpecSource no found")
    }

    override fun provideArguments(extensionContext: ExtensionContext?): Stream<out Arguments> {
        validateArguments()

        return getResources(extensionContext)
            .map { resource -> specDeserializer.readSpec(resource) }
            .flatMap { spec ->
                spec.testCases.map {
                    ZeebeSpecTestCase(
                        testCase = it
                    )
                }
            }
            .map { Arguments.of(it) }
            .stream()
    }

    private fun validateArguments() {
        if (specResources.isEmpty() && specDirectory == null) {
            throw RuntimeException("No spec resources or directory defined.")
        }

        if (specResources.isNotEmpty() && specDirectory != null) {
            throw RuntimeException("Both spec resources AND a directory are defined. But it can be either spec resources OR a directory.")
        }
    }

    private fun getResources(extensionContext: ExtensionContext?): List<InputStream> {
        if (specResources.isNotEmpty()) {
            return specResources
                .map { specResource ->
                    extensionContext
                        ?.testClass
                        ?.map { it.classLoader.getResourceAsStream(specResource) }
                        ?.orElseThrow { RuntimeException("No spec resource found with name '$specResource' in classpath") }
                        ?: throw RuntimeException("Extension context not found")
                }
        }

        if (specDirectory != null) {
            return specDirectory?.let { directory ->
                extensionContext
                    ?.testClass
                    ?.map { it.classLoader.getResource(specDirectory) }
                    ?.map { Path.of(it.toURI()) }
                    ?.filter { it.isDirectory() }
                    ?.map { dir ->
                        dir.toFile().listFiles { _, name -> name.endsWith(".yaml") }
                            ?.takeIf { it.isNotEmpty() }
                            ?.toList()
                            ?.map { FileInputStream(it) }
                            ?: throw RuntimeException("The spec directory with name '$directory' is empty. It must contain at least one spec resource.")
                    }
                    ?.orElseThrow { RuntimeException("No spec directory found with name '$directory' in classpath") }
                    ?: throw RuntimeException("Extension context not found")
            } ?: throw RuntimeException("No spec directory found")
        }

        throw RuntimeException("No spec resource provider found")
    }

}