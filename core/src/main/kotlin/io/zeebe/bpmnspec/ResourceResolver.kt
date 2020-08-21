package io.zeebe.bpmnspec

import java.io.InputStream

interface ResourceResolver {

    fun getResource(resourceName: String): InputStream
}