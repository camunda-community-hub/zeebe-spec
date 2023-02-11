package org.camunda.community.zeebe.spec.runner.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.worker.JobWorker
import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.runner.SpecActionExecutor
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration

class ZeebeSpecActionExecutor(
    zeebeClientProvider: () -> ZeebeClient
) : SpecActionExecutor {

    private val logger = LoggerFactory.getLogger(ZeebeSpecActionExecutor::class.java)

    private val jobWorkers = mutableListOf<JobWorker>()

    private val zeebeClient = zeebeClientProvider()

    override fun close() {
        logger.debug("Close job workers.")

        jobWorkers.map(JobWorker::close)
    }

    override fun deployProcess(name: String, bpmnXml: InputStream) {
        logger.debug("Deploy BPMN process. [name: {}]", name)

        zeebeClient
            .newDeployResourceCommand()
            .addResourceStream(bpmnXml, name)
            .send()
            .join()
    }

    override fun createProcessInstance(
        bpmnProcessId: String,
        variables: String
    ): ProcessInstanceKey {
        logger.debug(
            "Creating a process instance. [BPMN-process-id: {}, variables: {}]",
            bpmnProcessId,
            variables
        )

        val response = zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(bpmnProcessId)
            .latestVersion()
            .variables(variables)
            .send()
            .join()

        return response.processInstanceKey
    }

    override fun completeTask(jobType: String, variables: String) {
        logger.debug(
            "Start job worker to complete jobs. [job-type: {}, variables: {}]",
            jobType,
            variables
        )

        val jobWorker = zeebeClient
            .newWorker()
            .jobType(jobType)
            .handler { jobClient, job ->
                jobClient.newCompleteCommand(job.key)
                    .variables(variables)
                    .send()
                    .join()
            }
            .timeout(Duration.ofSeconds(1))
            .open()

        jobWorkers.add(jobWorker)
    }

    override fun publishMessage(messageName: String, correlationKey: String, variables: String) {
        logger.debug(
            "Publish message. [name: {}, correlation-key: {}, variables: {}]",
            messageName, correlationKey, variables
        )

        zeebeClient
            .newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationKey)
            .variables(variables)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }

    override fun throwError(jobType: String, errorCode: String, errorMessage: String) {
        logger.debug(
            "Start job worker to throw errors. [job-type: {}, error-code: {}, error-message: {}]",
            jobType, errorCode, errorMessage
        )

        val jobWorker = zeebeClient
            .newWorker()
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

        jobWorkers.add(jobWorker)
    }

    override fun cancelProcessInstance(processInstanceKey: ProcessInstanceKey) {
        logger.debug(
            "Cancel process instance. [key: {}]",
            processInstanceKey
        )

        zeebeClient
            .newCancelInstanceCommand(processInstanceKey)
            .send()
            .join()
    }

}