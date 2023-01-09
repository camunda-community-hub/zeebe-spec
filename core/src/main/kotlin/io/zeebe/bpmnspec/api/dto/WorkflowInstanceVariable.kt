package io.zeebe.bpmnspec.api.dto

data class ProcessInstanceVariable(
    val variableName: String,
    val variableValue: String,
    val scopeElementId: String,
    val scopeElementName: String?
)