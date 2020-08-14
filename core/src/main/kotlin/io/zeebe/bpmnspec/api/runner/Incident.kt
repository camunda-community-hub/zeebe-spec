package io.zeebe.bpmnspec.api.runner

data class Incident(
        val errorType: String,
        val errorMessage: String?,
        val elementId: String,
        val elementName: String?,
        val state: IncidentState
)