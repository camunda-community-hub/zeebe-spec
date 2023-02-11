package io.zeebe.bpmnspec

import java.io.File
import java.io.InputStream

interface ResourceResolver {

    fun getResource(resourceName: String): InputStream

    fun getResources(): List<File>
}