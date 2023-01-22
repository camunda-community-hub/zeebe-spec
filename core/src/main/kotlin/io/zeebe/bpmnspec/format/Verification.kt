package io.zeebe.bpmnspec.format

data class Verification(
    val verification: String,
    val args: Map<String, String>?
)