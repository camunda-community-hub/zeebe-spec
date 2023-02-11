package org.camunda.community.zeebe.spec.format

data class Verification(
    val verification: String,
    val args: Map<String, String>?
)