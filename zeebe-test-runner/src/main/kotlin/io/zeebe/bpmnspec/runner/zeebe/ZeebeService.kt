package io.zeebe.bpmnspec.runner.zeebe

import java.io.InputStream

interface ZeebeService : AutoCloseable {
    fun deployProcess(name: String, bpmnXml: InputStream)

    fun createProcessInstance(bpmnProcessId: String, variables: String): Long

    fun completeTask(jobType: String, variables: String)

    fun publishMessage(messageName: String, correlationKey: String, variables: String)

    fun throwError(jobType: String, errorCode: String, errorMessage: String)

    fun cancelProcessInstance(processInstanceKey: Long)
}
