package io.zeebe.bpmnspec.api.dto

data class ElementInstance(
    val elementId: String,
    val elementName: String?,
    val state: ElementInstanceState
)