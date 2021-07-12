package io.zeebe.bpmnspec.api.runner

data class ProcessInstanceVariable(
        val variableName: String,
        val variableValue: String,
        val scopeElementId: String,
        val scopeElementName: String?
)