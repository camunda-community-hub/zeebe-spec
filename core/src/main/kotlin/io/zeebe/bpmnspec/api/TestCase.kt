package io.zeebe.bpmnspec.api

data class TestCase(
        val name: String,
        val description: String?,
        val actions: List<Action>,
        val verifications: List<Verification>
) {
}