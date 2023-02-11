package org.camunda.community.zeebe.spec.api.dto

data class Incident(
    val errorType: String,
    val errorMessage: String?,
    val elementId: String,
    val elementName: String?,
    val state: IncidentState
)