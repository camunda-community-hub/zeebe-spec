package io.zeebe.bpmnspec.api

data class TestSpecResult(
        val spec: TestSpec,
        val testResults: List<TestResult>
)