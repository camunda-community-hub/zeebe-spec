package io.zeebe.bpmnspec

import java.io.File

interface ResourceResolver {

    fun getResources(): List<File>
}