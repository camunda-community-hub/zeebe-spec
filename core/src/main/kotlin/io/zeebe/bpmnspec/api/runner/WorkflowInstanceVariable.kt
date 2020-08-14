package io.zeebe.bpmnspec.api.runner

data class WorkflowInstanceVariable(
        val variableName: String,
        val variableValue: String,
        val scopeElementId: String,
        val scopeElementName: String?
)