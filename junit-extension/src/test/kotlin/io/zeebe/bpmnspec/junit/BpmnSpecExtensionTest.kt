package io.zeebe.bpmnspec.junit

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

//@ExtendWith(SpecRunnerFactoryProvider::class)
@BpmnSpecRunner(rootDirectory = "/spec", verificationTimeout = "PT5S")
class BpmnSpecExtensionTest(factory: SpecRunnerFactory) {

//    private val specRunner = SpecRunner(
//            testRunner = ZeebeTestRunner(),
//            resourceResolver = ClasspathResourceResolver(BpmnSpecExtensionTest::class.java.classLoader))

    private val specRunner = factory.create(testRunner = ZeebeTestRunner())


    @TestTemplate
    @ExtendWith(BpmnSpecContextProvider::class)
    @BpmnSpec(specResource = "exclusive-gateway-spec.yaml")
    fun `exclusive gateway`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

    @TestTemplate
    @ExtendWith(BpmnSpecContextProvider::class)
    @BpmnSpec(specResource = "boundary-event-spec.yaml")
    fun `boundary event`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

    @BeforeEach
    fun `before`() {
        println("before each")
    }

    @AfterEach
    fun `after`() {
        println("after each")
    }

    @ParameterizedTest
    @BpmnSpecSource(specResource = "exclusive-gateway-spec.yaml")
    fun `with parameterized test`(spec: BpmnSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(resources = spec.resources, testcase = spec.testCase)

        assertThat(testResult.success)
                .describedAs(testResult.message)
                .isTrue()
    }

    companion object {

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            println("beforeAll called")
        }
        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            println("afterAll called")
        }
    }

}
