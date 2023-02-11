package org.camunda.community.zeebe.spec

import java.io.File

interface ResourceResolver {

    fun getResources(): List<File>
}