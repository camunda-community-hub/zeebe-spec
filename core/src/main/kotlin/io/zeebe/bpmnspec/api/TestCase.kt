package io.zeebe.bpmnspec.api

data class TestCase(
    val name: String,
    val description: String?,
    val instructions: List<Instruction>
) {
}