package org.camunda.community.zeebe.spec.api

data class TestSpecResult(
    val spec: TestSpec,
    val testResults: List<TestResult>
)