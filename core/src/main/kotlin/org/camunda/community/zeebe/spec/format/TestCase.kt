package org.camunda.community.zeebe.spec.format

data class TestCase(
    val name: String,
    val description: String?,
    val actions: List<Action>?,
    val verifications: List<Verification>?,
    val instructions: List<Instruction>?
)