package io.zeebe.bpmnspec.builder

import com.fasterxml.jackson.databind.ObjectMapper
import io.zeebe.bpmnspec.actions.*
import io.zeebe.bpmnspec.api.*
import io.zeebe.bpmnspec.api.dto.ElementInstanceState
import io.zeebe.bpmnspec.api.dto.IncidentState
import io.zeebe.bpmnspec.api.dto.ProcessInstanceState
import io.zeebe.bpmnspec.verifications.*

@DslMarker
annotation class TestSpecMarker

fun testSpec(init: TestSpecBuilder.() -> Unit): TestSpec {
    val builder = TestSpecBuilder()
    builder.init()
    return builder.build()
}

@TestSpecMarker
class TestSpecBuilder {
    private val resources = mutableListOf<String>()
    private val testCases = mutableListOf<TestCase>();

    fun resources(vararg resources: String) {
        this.resources.addAll(resources)
    }

    fun testCase(name: String, description: String? = null, init: TestCaseBuilder.() -> Unit) {
        val testCaseBuilder = TestCaseBuilder(name, description)

        testCaseBuilder.init()

        testCases.add(testCaseBuilder.build())
    }

    fun build(): TestSpec {
        return TestSpec(resources, testCases);
    }
}

@TestSpecMarker
class TestCaseBuilder(private val name: String, private val description: String?) {
    private val instructions = mutableListOf<Instruction>()

    fun actions(init: ActionsBuilder.() -> Unit) {
        val actionsBuilder = ActionsBuilder();

        actionsBuilder.init()

        instructions.addAll(actionsBuilder.build())
    }

    fun verifications(init: VerificationsBuilder.() -> Unit) {
        val verificationsBuilder = VerificationsBuilder();

        verificationsBuilder.init()

        instructions.addAll(verificationsBuilder.build())
    }

    // Actions ============

    fun completeTask(jobType: String, variables: Map<String, Any> = emptyMap()) {
        instructions.add(CompleteTaskAction(jobType, serializeToJSON(variables)))
    }

    fun cancelInstance(processInstance: String? = null) {
        instructions.add(CancelInstanceAction(processInstance));
    }

    fun createInstance(
        bpmnProcessId: String,
        variables: Map<String, Any> = emptyMap(),
        processInstanceAlias: String? = null
    ) {
        instructions.add(
            CreateInstanceAction(
                bpmnProcessId,
                serializeToJSON(variables),
                processInstanceAlias
            )
        )
    }

    fun publishMessage(
        messageName: String,
        correlationKey: String,
        variables: Map<String, Any> = emptyMap()
    ) {
        instructions.add(
            PublishMessageAction(
                messageName,
                correlationKey,
                serializeToJSON(variables)
            )
        )
    }

    fun throwError(jobType: String, errorCode: String, errorMessage: String = "") {
        instructions.add(ThrowErrorAction(jobType, errorCode, errorMessage));
    }

    // Verifications ============

    fun verifyProcessInstanceState(
        state: ProcessInstanceState,
        processInstanceAlias: String? = null
    ) {
        instructions.add(ProcessInstanceStateVerification(state, processInstanceAlias))
    }

    fun verifyElementInstanceState(
        selector: ElementSelector,
        state: ElementInstanceState,
        processInstanceAlias: String? = null
    ) {
        instructions.add(
            ElementInstanceStateVerification(
                state,
                selector.elementId,
                selector.elementName,
                processInstanceAlias
            )
        )
    }

    fun verifyProcessInstanceVariable(
        variableName: String,
        value: Any,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        instructions.add(
            ProcessInstanceVariableVerification(
                variableName,
                serializeToJSON(value),
                processInstanceAlias,
                selector?.elementId,
                selector?.elementName
            )
        )
    }

    fun verifyProcessInstanceVariables(
        variables: Map<String, Any>,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        variables.forEach { (key, value) ->
            verifyProcessInstanceVariable(
                key,
                value,
                selector,
                processInstanceAlias
            )
        };
    }

    fun verifyNoProcessInstanceVariable(
        variableName: String,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        instructions.add(
            NoProcessInstanceVariableVerification(
                variableName,
                processInstanceAlias,
                selector?.elementId,
                selector?.elementName
            )
        )
    }

    fun verifyIncidentState(
        state: IncidentState,
        errorType: String,
        errorMessage: String?,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        instructions.add(
            IncidentStateVerification(
                state,
                errorType,
                errorMessage,
                selector?.elementId,
                selector?.elementName,
                processInstanceAlias
            )
        )
    }


    fun build(): TestCase {
        return TestCase(name, description, instructions)
    }
}

@TestSpecMarker
class ActionsBuilder {
    private val actions = mutableListOf<Action>()

    fun awaitElementInstanceState(
        selector: ElementSelector,
        state: ElementInstanceState,
        processInstanceAlias: String? = null
    ) {
        actions.add(
            AwaitElementInstanceStateAction(
                state,
                selector.elementId,
                selector.elementName,
                processInstanceAlias
            )
        )
    }

    fun cancelInstance(processInstance: String? = null) {
        actions.add(CancelInstanceAction(processInstance));
    }

    fun completeTask(jobType: String, variables: Map<String, Any> = emptyMap()) {
        actions.add(CompleteTaskAction(jobType, serializeToJSON(variables)))
    }

    fun createInstance(
        bpmnProcessId: String,
        variables: Map<String, Any> = emptyMap(),
        processInstanceAlias: String? = null
    ) {
        actions.add(
            CreateInstanceAction(
                bpmnProcessId,
                serializeToJSON(variables),
                processInstanceAlias
            )
        )
    }

    fun publishMessage(
        messageName: String,
        correlationKey: String,
        variables: Map<String, Any> = emptyMap()
    ) {
        actions.add(PublishMessageAction(messageName, correlationKey, serializeToJSON(variables)))
    }

    fun throwError(jobType: String, errorCode: String, errorMessage: String = "") {
        actions.add(ThrowErrorAction(jobType, errorCode, errorMessage));
    }

    fun build(): List<Action> {
        return actions;
    }
}

@TestSpecMarker
class VerificationsBuilder {
    private val verifications = mutableListOf<Verification>()

    fun elementInstanceState(
        selector: ElementSelector,
        state: ElementInstanceState,
        processInstanceAlias: String? = null
    ) {
        verifications.add(
            ElementInstanceStateVerification(
                state,
                selector.elementId,
                selector.elementName,
                processInstanceAlias
            )
        )
    }

    fun processInstanceState(state: ProcessInstanceState, processInstanceAlias: String? = null) {
        verifications.add(ProcessInstanceStateVerification(state, processInstanceAlias))
    }

    fun processInstanceVariable(
        variableName: String,
        value: Any,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        verifications.add(
            ProcessInstanceVariableVerification(
                variableName,
                serializeToJSON(value),
                processInstanceAlias,
                selector?.elementId,
                selector?.elementName
            )
        )
    }

    fun processInstanceVariables(
        variables: Map<String, Any>,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        variables.forEach { (key, value) ->
            processInstanceVariable(
                key,
                value,
                selector,
                processInstanceAlias
            )
        };
    }

    fun noProcessInstanceVariable(
        variableName: String,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        verifications.add(
            NoProcessInstanceVariableVerification(
                variableName,
                processInstanceAlias,
                selector?.elementId,
                selector?.elementName
            )
        )
    }

    fun incidentState(
        state: IncidentState,
        errorType: String,
        errorMessage: String?,
        selector: ElementSelector? = null,
        processInstanceAlias: String? = null
    ) {
        verifications.add(
            IncidentStateVerification(
                state,
                errorType,
                errorMessage,
                selector?.elementId,
                selector?.elementName,
                processInstanceAlias
            )
        )
    }

    fun build(): List<Verification> {
        return verifications
    }
}

private val objectMapper = ObjectMapper()

private fun serializeToJSON(input: Any): String {
    return objectMapper.writeValueAsString(input);
}