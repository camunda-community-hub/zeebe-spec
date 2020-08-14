package io.zeebe.bpmnspec.api.runner

data class ElementInstance(
        val elementId: String,
        val elementName: String?,
        val state: ElementInstanceState
)