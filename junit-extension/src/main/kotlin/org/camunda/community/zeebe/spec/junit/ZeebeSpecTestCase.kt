package org.camunda.community.zeebe.spec.junit

import org.camunda.community.zeebe.spec.api.TestCase

data class ZeebeSpecTestCase(val testCase: TestCase) {

    override fun toString(): String {
        return "${testCase.name}${
            testCase.description?.takeIf { it.isNotEmpty() }?.let { " ($it)" }
        }"
    }
}