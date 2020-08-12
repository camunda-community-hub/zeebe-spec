package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.WorkflowInstanceContext

data class ZeebeWorkflowInstanceContext(
        val workflowInstanceKey: Long
) : WorkflowInstanceContext