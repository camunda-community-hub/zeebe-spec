package org.camunda.community.zeebe.spec

import java.io.File
import java.io.FilenameFilter
import java.nio.file.Path
import kotlin.io.path.isDirectory

class ClasspathResourceResolver(
    private val classLoader: ClassLoader,
    private val rootDirectory: String = ""
) : org.camunda.community.zeebe.spec.ResourceResolver {

    override fun getResources(): List<File> {
        return classLoader.getResource(rootDirectory)
            ?.let { Path.of(it.toURI()) }
            ?.takeIf { it.isDirectory() }
            ?.let { dir ->
                dir.toFile().listFiles(deploymentFilter())
                    ?.toList()
            }
            ?: throw RuntimeException("No resource directory found with name '${rootDirectory}'")
    }

    private fun deploymentFilter(): FilenameFilter {
        return FilenameFilter { _, name ->
            name.endsWith(".bpmn") || name.endsWith(".dmn")
        }
    }
}