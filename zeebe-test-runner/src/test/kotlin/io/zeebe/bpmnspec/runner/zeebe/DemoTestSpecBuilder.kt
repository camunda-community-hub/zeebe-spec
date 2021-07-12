package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.api.TestSpec
import io.zeebe.bpmnspec.api.runner.ElementInstanceState
import io.zeebe.bpmnspec.api.runner.IncidentState
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import io.zeebe.bpmnspec.builder.ElementSelector.Companion.byId
import io.zeebe.bpmnspec.builder.ElementSelector.Companion.byName
import io.zeebe.bpmnspec.builder.testSpec

class DemoTestSpecBuilder {

    companion object {
        fun demo(): TestSpec {
            return testSpec {
                resources("demo.bpmn")
                testCase(name = "complete process", description = "demo test case") {
                    actions {
                        createInstance("demo")
                        completeTask("a")
                        completeTask("b")
                        completeTask("c")
                    }
                    verifications {
                        processInstanceState(ProcessInstanceState.COMPLETED)
                    }
                }
            }
        }

        fun demo2(): TestSpec {
            return testSpec {
                resources("demo.bpmn")
                testCase(name = "complete process", description = "demo test case") {
                    actions {
                        createInstance(bpmnProcessId = "demo", processInstanceAlias = "process-1")
                        createInstance(bpmnProcessId = "demo", processInstanceAlias = "process-2")
                        completeTask("a")
                        completeTask("b")
                        completeTask("c")
                    }
                    verifications {
                        processInstanceState(
                            state = ProcessInstanceState.COMPLETED,
                            processInstanceAlias = "process-1"
                        )
                        processInstanceState(
                            state = ProcessInstanceState.TERMINATED,
                            processInstanceAlias = "process-2"
                        )
                    }
                }
            }
        }

        fun demo3(): TestSpec {
            return testSpec {
                resources("demo2.bpmn")
                testCase("publish message") {
                    actions {
                        createInstance(
                            bpmnProcessId = "demo2",
                            variables = mapOf("key" to "key-1"),
                            processInstanceAlias = "process-1"
                        )
                        awaitElementInstanceState(
                            byName("message-1"),
                            ElementInstanceState.ACTIVATED
                        )
                        publishMessage(
                            messageName = "message-1",
                            correlationKey = "key-1",
                            variables = mapOf("x" to 1)
                        )
                        throwError(jobType = "b", errorCode = "error-1")
                        completeTask("c")
                    }
                    verifications {
                        processInstanceState(ProcessInstanceState.COMPLETED)
                        elementInstanceState(
                            byId("Activity_1g1az2f"),
                            ElementInstanceState.COMPLETED
                        )
                        elementInstanceState(byName("b"), ElementInstanceState.TERMINATED)
                        processInstanceVariable(variableName = "x", value = "1")
                        noProcessInstanceVariable(variableName = "x", byName("message-1"))
                    }
                }

            }
        }

        fun demoIncident(): TestSpec {
            return testSpec {
                resources("demo2.bpmn")
                testCase("publish message") {
                    actions {
                        createInstance("demo2")
                        completeTask("b")
                        completeTask("c")
                    }
                    verifications {
                        processInstanceState(ProcessInstanceState.ACTIVATED)
                        incidentState(
                            state = IncidentState.CREATED,
                            errorType = "EXTRACT_VALUE_ERROR",
                            errorMessage = "failed to evaluate expression 'key': no variable found for name 'key'",
                            byName("message-1")
                        )
                    }
                }
            }
        }

        fun failedTestCase(): TestSpec {
            return testSpec {
                resources("demo.bomn")
                testCase("fail-verification") {
                    actions {
                        createInstance("demo")
                        completeTask("a")
                    }
                    verifications {
                        elementInstanceState(byName("A"), ElementInstanceState.COMPLETED)
                        elementInstanceState(byName("B"), ElementInstanceState.COMPLETED)
                        elementInstanceState(byName("B"), ElementInstanceState.COMPLETED)
                    }
                }
            }
        }
    }
}