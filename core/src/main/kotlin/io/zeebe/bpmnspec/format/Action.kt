package io.zeebe.bpmnspec.format

data class Action(
    val action: String,
    val args: Map<String, String>?
)