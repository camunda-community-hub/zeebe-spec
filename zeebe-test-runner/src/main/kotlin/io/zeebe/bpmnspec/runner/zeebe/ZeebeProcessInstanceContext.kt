package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.ProcessInstanceContext

data class ZeebeProcessInstanceContext(
        val processInstanceKey: Long
) : ProcessInstanceContext