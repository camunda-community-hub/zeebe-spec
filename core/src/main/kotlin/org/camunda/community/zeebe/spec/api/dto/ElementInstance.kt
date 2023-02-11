package org.camunda.community.zeebe.spec.api.dto

data class ElementInstance(
    val elementId: String,
    val elementName: String?,
    val state: ElementInstanceState
)