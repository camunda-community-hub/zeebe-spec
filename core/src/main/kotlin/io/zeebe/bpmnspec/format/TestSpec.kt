package io.zeebe.bpmnspec.format

data class TestSpec(
        val resources: List<String>,
        val testCases: List<TestCase>
)