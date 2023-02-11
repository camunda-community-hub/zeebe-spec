package io.zeebe.bpmnspec

import java.io.File
import java.io.FilenameFilter
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class DirectoryResourceResolver(
    private val rootDirectory: Path
) : ResourceResolver {

    override fun getResource(resourceName: String): InputStream {
        return rootDirectory.resolve(resourceName).let { Files.newInputStream(it) }
            ?: throw RuntimeException("No resource found found with name '$resourceName' in the directory '${rootDirectory.toAbsolutePath()}'")
    }

    override fun getResources(): List<File> {
        return rootDirectory
            .takeIf { it.isDirectory() }
            ?.let { dir ->
                dir.toFile().listFiles(deploymentFilter())
                    ?.toList()
            }
            ?: throw RuntimeException("No resource directory found with name '${rootDirectory.name}'")
    }

    private fun deploymentFilter(): FilenameFilter {
        return FilenameFilter { _, name ->
            name.endsWith(".bpmn") || name.endsWith(".dmn")
        }
    }
}