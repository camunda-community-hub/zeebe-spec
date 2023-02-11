package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.api.TestCase

data class BpmnSpecTestCase(val testCase: TestCase) {

    override fun toString(): String {
        return "${testCase.name}${
            testCase.description?.takeIf { it.isNotEmpty() }?.let { " ($it)" }
        }"
    }
}