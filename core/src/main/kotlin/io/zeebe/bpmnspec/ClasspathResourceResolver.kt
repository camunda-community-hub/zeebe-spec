package io.zeebe.bpmnspec

import java.io.InputStream

class ClasspathResourceResolver(
        private val classLoader: ClassLoader) : ResourceResolver {

    override fun getResource(resourceName: String): InputStream {
        return classLoader.getResourceAsStream(resourceName)
                ?: throw RuntimeException("no resource found with name '$resourceName' in the classpath '${classLoader.name}'")
    }
}