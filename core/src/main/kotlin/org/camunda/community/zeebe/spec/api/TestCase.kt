package org.camunda.community.zeebe.spec.api

data class TestCase(
    val name: String,
    val description: String?,
    val instructions: List<Instruction>
) {
}