package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.TestRunner
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsVerifications
import io.zeebe.client.ZeebeClient
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration

class ZeebeRunner : TestRunner {

    val logger = LoggerFactory.getLogger(ZeebeRunner::class.java)

    var client: ZeebeClient = ZeebeClient
            .newClientBuilder()
            .brokerContactPoint("localhost:26500")
            .usePlaintext()
            .build()

    val zeeqsVerifications = ZeeqsVerifications()

    override fun init() {
//        client = ZeebeClient
//                .newClientBuilder()
//                .brokerContactPoint("localhost:26500")
//                .usePlaintext()
//                .build()
    }

    override fun cleanUp() {
        TODO("Not yet implemented")
    }

    override fun deployWorkflow(name: String, bpmnXml: InputStream) {
        client.newDeployCommand()
                .addResourceStream(bpmnXml, name)
                .send()
                .join()
    }

    override fun createWorkflowInstance(bpmnProcessId: String, variables: String): WorkflowInstanceContext {
        val response = client.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion().variables(variables)
                .send()
                .join()

        return ZeebeWorkflowInstanceContext(
                workflowInstanceKey = response.workflowInstanceKey
        )
    }

    override fun completeTask(jobType: String, variables: String) {
        client.newWorker()
                .jobType(jobType)
                .handler { jobClient, job ->
                    jobClient.newCompleteCommand(job.key)
                            .variables(variables)
                            .send()
                            .join()
                }
                .timeout(Duration.ofSeconds(1))
                .open()
    }

    override fun getWorkflowInstanceState(context: WorkflowInstanceContext): WorkflowInstanceState {

        val wfContext = context as ZeebeWorkflowInstanceContext

        val state = zeeqsVerifications.getWorkflowInstanceState(wfContext.workflowInstanceKey)
        return when (state) {
            "COMPLETED" -> WorkflowInstanceState.COMPLETED
            "TERMINATED" -> WorkflowInstanceState.TERMINATED
            else -> WorkflowInstanceState.ACTIVATED
        }
    }

}