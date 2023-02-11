package org.camunda.community.zeebe.spec

import org.camunda.community.zeebe.spec.actions.CompleteTaskAction
import org.camunda.community.zeebe.spec.actions.CreateInstanceAction
import org.camunda.community.zeebe.spec.format.SpecDeserializer
import org.camunda.community.zeebe.spec.verifications.ElementInstanceStateVerification
import org.camunda.community.zeebe.spec.verifications.ProcessInstanceStateVerification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecFormatTest {

    @Test
    fun `should deserialize spec (actions-verifications-style)`() {

        val deserializer = SpecDeserializer()

        val input =
            SpecFormatTest::class.java.getResourceAsStream("/demo-actions-verifications-style.yaml")
        val spec = deserializer.readSpec(input)

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
    fun `should deserialize spec (instructions-style)`() {

        val deserializer = SpecDeserializer()

        val input = SpecFormatTest::class.java.getResourceAsStream("/spec-instructions-style.yaml")
        val spec = deserializer.readSpec(input)

        assertThat(spec.testCases).hasSize(1)

        val testCase = spec.testCases[0]
        assertThat(testCase.name).isEqualTo("complete process")
        assertThat(testCase.description).isEqualTo("Use instructions")

        assertThat(testCase.instructions).hasSize(4)
        assertThat(testCase.instructions[0]).isInstanceOf(CreateInstanceAction::class.java)
        assertThat(testCase.instructions[1]).isInstanceOf(ElementInstanceStateVerification::class.java)
        assertThat(testCase.instructions[2]).isInstanceOf(CompleteTaskAction::class.java)
        assertThat(testCase.instructions[3]).isInstanceOf(ElementInstanceStateVerification::class.java)
    }

}