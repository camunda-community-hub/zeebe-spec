package io.zeebe.bpmnspec

import java.io.File
import java.io.FilenameFilter
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class DirectoryResourceResolver(
    private val rootDirectory: Path
) : ResourceResolver {

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