package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action

data class CreateInstanceAction(
        val bpmnProcessId: String,
        val variables: String
) : Action