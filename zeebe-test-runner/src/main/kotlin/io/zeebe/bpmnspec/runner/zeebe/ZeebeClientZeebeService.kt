package io.zeebe.bpmnspec.runner.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.worker.JobWorker
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock

class ZeebeClientZeebeService(
    private val zeebeClient: ZeebeClient
) : ZeebeService {

    private val lock: ReentrantLock = ReentrantLock()
    private val workers: MutableList<JobWorker> = object : ArrayList<JobWorker>() {
        override fun add(element: JobWorker): Boolean {
            lock.lock()
            try {
                return super.add(element)
            } finally {
                lock.unlock()
            }
        }
    }

    override fun deployProcess(name: String, bpmnXml: InputStream) {
        zeebeClient.newDeployResourceCommand()
            .addResourceStream(bpmnXml, name)
            .send()
            .join()
    }

    override fun createProcessInstance(bpmnProcessId: String, variables: String): Long {
        return zeebeClient.newCreateInstanceCommand()
            .bpmnProcessId(bpmnProcessId)
            .latestVersion().variables(variables)
            .send()
            .join()
            .processInstanceKey
    }

    override fun completeTask(jobType: String, variables: String) {
        val jobWorker = zeebeClient.newWorker()
            .jobType(jobType)
            .handler { jobClient, job ->
                jobClient.newCompleteCommand(job.key)
                    .variables(variables)
                    .send()
                    .join()
            }
            .timeout(Duration.ofSeconds(1))
            .open()

        workers.add(jobWorker)
    }

    override fun publishMessage(messageName: String, correlationKey: String, variables: String) {
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationKey)
            .variables(variables)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }

    override fun throwError(jobType: String, errorCode: String, errorMessage: String) {
        val jobWorker = zeebeClient.newWorker()
            .jobType(jobType)
            .handler { jobClient, job ->
                jobClient.newThrowErrorCommand(job.key)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .send()
                    .join()
            }
            .timeout(Duration.ofSeconds(1))
            .open()

        workers.add(jobWorker)
    }

    override fun cancelProcessInstance(processInstanceKey: Long) {
        zeebeClient.newCancelInstanceCommand(processInstanceKey)
            .send()
            .join()
    }

    override fun close() = workers.forEach { it.close() }
}
