package io.zeebe.bpmnspec.api

data class TestSpec(
        val resources: List<String>,
        val testCases: List<TestCase>
)