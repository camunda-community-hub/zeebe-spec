package org.camunda.community.zeebe.spec.api.dto

data class ProcessInstanceVariable(
    val variableName: String,
    val variableValue: String,
    val scopeElementId: String,
    val scopeElementName: String?
)