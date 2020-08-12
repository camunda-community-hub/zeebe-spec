package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action

data class CompleteTaskAction(
        val jobType: String,
        val variables: String
) : Action