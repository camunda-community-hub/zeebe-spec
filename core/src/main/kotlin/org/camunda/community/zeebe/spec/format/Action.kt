package org.camunda.community.zeebe.spec.format

data class Action(
    val action: String,
    val args: Map<String, String>?
)