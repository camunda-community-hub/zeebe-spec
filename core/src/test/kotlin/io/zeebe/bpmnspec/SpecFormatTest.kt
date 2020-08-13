package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.actions.CompleteTaskAction
import io.zeebe.bpmnspec.api.actions.CreateInstanceAction
import io.zeebe.bpmnspec.api.verifications.WorkflowInstanceStateVerification
import io.zeebe.bpmnspec.format.SpecDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecFormatTest {

    @Test
    fun `should deserialize spec`() {

        val deserializer = SpecDeserializer()

        val input = SpecFormatTest::class.java.getResourceAsStream("/demo.yaml")
        val spec = deserializer.readSpec(input)

        assertThat(spec.resources)
                .hasSize(1)
                .contains("demo.bpmn")

        assertThat(spec.testCases).hasSize(1)

        val testCase = spec.testCases[0]
        assertThat(testCase.name).isEqualTo("complete workflow")
        assertThat(testCase.description).isEqualTo("demo test case")

        assertThat(testCase.actions).hasSize(4)
        assertThat(testCase.actions[0]).isInstanceOf(CreateInstanceAction::class.java)
        assertThat(testCase.actions[1]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.actions[2]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.actions[3]).isInstanceOf(CompleteTaskAction::class.java)

        assertThat(testCase.verifications).hasSize(1)
        assertThat(testCase.verifications[0]).isInstanceOf(WorkflowInstanceStateVerification::class.java)
    }

}