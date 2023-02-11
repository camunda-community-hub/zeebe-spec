package org.camunda.community.zeebe.spec.format

data class TestSpec(
    @Deprecated("Explicit resources are not supported anymore.")
    val resources: List<String>? = emptyList(),
    val testCases: List<TestCase>
)