package io.zeebe.bpmnspec.runner.eze

import io.zeebe.bpmnspec.api.ProcessInstanceContext

data class EzeProcessInstanceContext(
    val processInstanceKey: Long
) : ProcessInstanceContext {
}