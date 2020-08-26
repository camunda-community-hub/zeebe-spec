package io.zeebe.bpmnspec

import java.io.InputStream

class ClasspathResourceResolver(
        private val classLoader: ClassLoader,
        private val rootDirectory: String = ""
) : ResourceResolver {

    override fun getResource(resourceName: String): InputStream {

        val resource = if (rootDirectory.isBlank()) {
            resourceName
        } else {
            "$rootDirectory/$resourceName"
        }

        return classLoader.getResourceAsStream(resource)
                ?: throw RuntimeException("no resource found with name '$resource' in the classpath '${classLoader.name}'")
    }
}