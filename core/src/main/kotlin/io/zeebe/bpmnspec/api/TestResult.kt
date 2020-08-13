package io.zeebe.bpmnspec.api

data class TestResult(
        val testCase: TestCase,
        val success: Boolean,
        val message: String
)