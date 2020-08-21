package io.zeebe.bpmnspec

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class DirectoryResourceResolver(
        private val rootDirectory: Path) : ResourceResolver {

    override fun getResource(resourceName: String): InputStream {
        return rootDirectory.resolve(resourceName).let { Files.newInputStream(it) }
                ?: throw RuntimeException("no resource found found with name '$resourceName' in the directory '${rootDirectory.toAbsolutePath()}'")
    }
}