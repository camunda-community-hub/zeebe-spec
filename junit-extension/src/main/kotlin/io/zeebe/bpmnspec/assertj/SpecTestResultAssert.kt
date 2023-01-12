package io.zeebe.bpmnspec.assertj

import io.zeebe.bpmnspec.api.TestOutput
import io.zeebe.bpmnspec.api.TestResult
import org.assertj.core.api.AbstractAssert

class SpecTestResultAssert(actual: TestResult) :
    AbstractAssert<SpecTestResultAssert, TestResult>(actual, SpecTestResultAssert::class.java) {

    companion object {
        fun assertThat(actual: TestResult): SpecTestResultAssert =
            SpecTestResultAssert(actual = actual)
    }

    fun isSuccessful(): SpecTestResultAssert {
        isNotNull()

        if (!actual.success) {
            val testOutput = actual.output.joinToString(separator = "\n") {
                formatTestOutput(it)
            }

            failWithMessage(
                """
                %s
                
                Test output:
                ============
                
                %s
            """.trimIndent(), actual.message, testOutput
            )
        }

        return this
    }

    private fun formatTestOutput(testOutput: TestOutput): String {

        val elementInstances = testOutput.elementInstances.map { instance ->
            "\t- '${instance.elementName ?: instance.elementId}': ${instance.state}"
        }.joinToString(separator = "\n", prefix = "\n")

        val variables = testOutput.variables.map { variable ->
            "\t- '${variable.variableName}': '${variable.variableValue}' [scope: '${variable.scopeElementName ?: variable.scopeElementId}']"
        }.joinToString(separator = "\n", prefix = "\n")

        val incidents = testOutput.incidents.map { incident ->
            "\t- ${incident.errorType}: '${incident.errorMessage}' [state: ${incident.state}, scope: '${incident.elementName ?: incident.elementId}']"
        }.joinToString(separator = "\n", prefix = "\n")

        return """Process instance key: ${testOutput.processInstanceKey}
                        |State: ${testOutput.state}
                        |Element instances: $elementInstances
                        |Variables: $variables
                        |Incidents: $incidents""".trimMargin()
    }

}