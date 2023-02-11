package org.camunda.community.zeebe.spec

import org.camunda.community.zeebe.spec.api.dto.ElementInstanceState
import org.camunda.community.zeebe.spec.api.dto.ProcessInstanceState
import org.camunda.community.zeebe.spec.builder.ElementSelector
import org.camunda.community.zeebe.spec.builder.testSpec
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class SpecBuilderTest {

    private val resourceResolver =
        org.camunda.community.zeebe.spec.ClasspathResourceResolver(classLoader = SpecTestRunnerTest::class.java.classLoader)

    private val specRunner = SpecRunner(
        resourceResolver = resourceResolver,
        verificationTimeout = Duration.ofSeconds(3)
    )

    @Test
    fun `should run Kotlin spec (instructions-style)`() {
        // given
        val spec = testSpec {
            testCase(name = "complete process", description = "demo test case") {

                createInstance(bpmnProcessId = "demo")

                completeTask(jobType = "a")
                completeTask(jobType = "b")
                completeTask(jobType = "c")

                verifyElementInstanceState(
                    selector = ElementSelector.byName("A"),
                    state = ElementInstanceState.COMPLETED
                )

                verifyProcessInstanceState(ProcessInstanceState.COMPLETED)
            }
        }
        // when
        val result = specRunner.runSpec(spec)
        // then
        Assertions.assertThat(result.testResults)
            .hasSize(1)
            .allSatisfy {
                Assertions.assertThat(it.success).isTrue()
            }
    }

    @Test
    fun `should run Kotlin spec (actions-verifications-style)`() {
        // given
        val spec = testSpec {
            testCase(name = "complete process", description = "demo test case") {
                actions {
                    createInstance(bpmnProcessId = "demo")

                    completeTask(jobType = "a")
                    completeTask(jobType = "b")
                    completeTask(jobType = "c")
                }

                verifications {
                    elementInstanceState(
                        selector = ElementSelector.byName("A"),
                        state = ElementInstanceState.COMPLETED
                    )

                    processInstanceState(state = ProcessInstanceState.COMPLETED)
                }
            }
        }
        // when
        val result = specRunner.runSpec(spec)
        // then
        Assertions.assertThat(result.testResults)
            .hasSize(1)
            .allSatisfy {
                Assertions.assertThat(it.success).isTrue()
            }
    }

}