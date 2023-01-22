package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.actions.CompleteTaskAction
import io.zeebe.bpmnspec.actions.CreateInstanceAction
import io.zeebe.bpmnspec.format.SpecDeserializer
import io.zeebe.bpmnspec.verifications.ProcessInstanceStateVerification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecFormatTest {

    @Test
    fun `should deserialize spec with actions and verifications`() {

        val deserializer = SpecDeserializer()

        val input = SpecFormatTest::class.java.getResourceAsStream("/demo.yaml")
        val spec = deserializer.readSpec(input)

        assertThat(spec.resources)
            .hasSize(1)
            .contains("demo.bpmn")

        assertThat(spec.testCases).hasSize(1)

        val testCase = spec.testCases[0]
        assertThat(testCase.name).isEqualTo("complete process")
        assertThat(testCase.description).isEqualTo("demo test case")

        assertThat(testCase.instructions).hasSize(5)
        assertThat(testCase.instructions[0]).isInstanceOf(CreateInstanceAction::class.java)
        assertThat(testCase.instructions[1]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[2]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[3]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[4]).isInstanceOf(ProcessInstanceStateVerification::class.java)
    }

    @Test
    fun `should deserialize spec with instructions`() {

        val deserializer = SpecDeserializer()

        val input = SpecFormatTest::class.java.getResourceAsStream("/spec-with-instructions.yaml")
        val spec = deserializer.readSpec(input)

        assertThat(spec.testCases).hasSize(1)

        val testCase = spec.testCases[0]
        assertThat(testCase.name).isEqualTo("complete process")
        assertThat(testCase.description).isEqualTo("Use instructions")

        assertThat(testCase.instructions).hasSize(5)
        assertThat(testCase.instructions[0]).isInstanceOf(CreateInstanceAction::class.java)
        assertThat(testCase.instructions[1]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[2]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[3]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[4]).isInstanceOf(ProcessInstanceStateVerification::class.java)
    }

}