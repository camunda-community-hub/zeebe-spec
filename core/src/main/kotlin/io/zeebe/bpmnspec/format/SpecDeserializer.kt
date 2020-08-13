package io.zeebe.bpmnspec.format

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.bpmnspec.api.actions.CompleteTaskAction
import io.zeebe.bpmnspec.api.actions.CreateInstanceAction
import io.zeebe.bpmnspec.api.actions.PublishMessageAction
import io.zeebe.bpmnspec.api.runner.WorkflowInstanceState
import io.zeebe.bpmnspec.api.verifications.WorkflowInstanceStateVerification
import java.io.InputStream

class SpecDeserializer {

    fun readSpec(input: InputStream): io.zeebe.bpmnspec.api.TestSpec {

        val objectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

        val spec = objectMapper.readValue<TestSpec>(input)

        return io.zeebe.bpmnspec.api.TestSpec(
                resources = spec.resources,
                testCases = spec.testCases.map(this::transformTestCase)
        )
    }

    private fun transformTestCase(testCase: TestCase): io.zeebe.bpmnspec.api.TestCase {
        return io.zeebe.bpmnspec.api.TestCase(
                name = testCase.name,
                description = testCase.description,
                actions = testCase.actions.map(this::transformAction),
                verifications = testCase.verifications.map(this::transformVerification)
        )
    }

    private fun transformAction(action: Action): io.zeebe.bpmnspec.api.Action {
        val name = action.action.toLowerCase()
        val args = action.args

        return when (name) {
            "create-instance" -> CreateInstanceAction(
                    bpmnProcessId = args["bpmn_process_id"]
                            ?: throw RuntimeException("Missing required parameter 'bpmn_process_id' for action 'create-instance'"),
                    variables = args["variables"] ?: "{}",
                    workflowInstanceAlias = args["workflow_instance_alias"]
            )
            "complete-task" -> CompleteTaskAction(
                    jobType = args["job_type"]
                            ?: throw RuntimeException("Missing required parameter 'job_type' for action 'complete-task'"),
                    variables = args["variables"] ?: "{}"
            )
            "publish-message" -> PublishMessageAction(
                    messageName = args["message_name"] ?: throw RuntimeException("Missing required parameter 'message_name' for action 'publish-message'"),
                    correlationKey = args["correlation_key"] ?: throw RuntimeException("Missing required parameter 'correlation_key' for action 'publish-message'"),
                    variables = args["variables"] ?: "{}"
            )
            else -> throw RuntimeException("Unknown action '$name'")
        }
    }

    private fun transformVerification(verification: Verification): io.zeebe.bpmnspec.api.Verification {
        val name = verification.verification.toLowerCase()
        val args = verification.args

        return when (name) {
            "workflow-instance-state" -> WorkflowInstanceStateVerification(
                    state = args["state"]?.let { WorkflowInstanceState.valueOf(it.toUpperCase()) }
                            ?: throw RuntimeException("Missing required parameter 'state' for verification 'workflow-instance-state'"),
                    workflowInstance = args["workflow_instance"]
            )
            else -> throw RuntimeException("Unknown verification: '$name'")
        }
    }

}