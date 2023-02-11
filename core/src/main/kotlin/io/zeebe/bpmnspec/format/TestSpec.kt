package io.zeebe.bpmnspec.format

data class TestSpec(
    val resources: List<String>? = emptyList(),
    val testCases: List<TestCase>
)