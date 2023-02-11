package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.*
import io.zeebe.bpmnspec.format.SpecDeserializer
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider
import io.zeebe.bpmnspec.runner.TestRunnerEnvironment
import io.zeebe.bpmnspec.runner.eze.EzeEnvironment
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class SpecRunner(
    private val environment: TestRunnerEnvironment = EzeEnvironment(),
    private val beforeEachCallback: (SpecTestRunnerContext) -> Unit = {},
    private val afterEachCallback: (SpecTestRunnerContext) -> Unit = {},
    private val resourceResolver: ResourceResolver = DirectoryResourceResolver(
        rootDirectory = Path.of(
            "."
        )
    ),
    private val verificationTimeout: Duration = Duration.ofSeconds(10),
    private val verificationRetryInterval: Duration = Duration.ofMillis(10)
) {

    private val logger = LoggerFactory.getLogger(SpecRunner::class.java)

    private val specDeserializer = SpecDeserializer()

    private val actionExecutor: SpecActionExecutor = environment.actionExecutor
    private val stateProvider: SpecStateProvider = environment.stateProvider

    fun runSpec(input: InputStream): TestSpecResult {

        logger.trace("Read the spec")
        val spec = specDeserializer.readSpec(input)

        return runSpec(spec)
    }

    fun runSpec(spec: TestSpec): TestSpecResult {
        logger.debug("Run {} tests", spec.testCases.size)

        val testResults = spec.testCases.map { runTestCase(testcase = it) }

        logger.debug(
            "All tests finished [{}/{} passed]",
            testResults.filter { it.success }.count(),
            testResults.size
        )

        return TestSpecResult(
            spec = spec,
            testResults = testResults
        )
    }

    fun runSingleTestCase(testcase: TestCase): TestResult {
        logger.debug("Run a single test")

        return runTestCase(testcase = testcase)
    }

    private fun runTestCase(testcase: TestCase): TestResult {
        logger.debug("Prepare the test [name: '{}']", testcase.name)

        logger.debug("Create spec test environment")
        environment.create()

        logger.debug("Invoke before-each callback")
        beforeEachCallback(environment.getContext())

        resourceResolver.getResources()
            .map { it.name }
            .joinToString()
            .let {
                logger.debug(
                    "Deploy resource for the test. [name: '{}', resources: {}]",
                    testcase.name,
                    it
                )
            }

        resourceResolver.getResources().forEach { resource ->
            val resourceName = resource.name
            val resourceStream = FileInputStream(resource)

            actionExecutor.deployProcess(resourceName, resourceStream)
        }

        logger.debug(
            "Run the test [name: '{}', description: '{}']",
            testcase.name,
            testcase.description
        )
        val result = executeTestCase(testcase)

        logger.debug(
            "Test finished [name: '{}', success: '{}', message: '{}']",
            testcase.name,
            result.success,
            result.message
        )

        logger.debug("Invoke after-each callback")
        afterEachCallback(environment.getContext())

        logger.debug("Close spec test environment")
        environment.close()

        return result
    }

    private fun executeTestCase(testcase: TestCase): TestResult {

        val contexts = mutableMapOf<String, ProcessInstanceKey>()

        // TODO (saig0): handle case when the context is not found
        val testContext = TestContext(
            storeContext = { alias, context -> contexts.put(alias, context) },
            getContext = { alias -> alias.let { contexts[it] } ?: contexts.values.first() },
            verificationTimeout = verificationTimeout,
            verificationRetryInterval = verificationRetryInterval
        )

        val successfulVerifications = mutableListOf<Verification>()

        testcase.instructions.forEach { instruction ->

            if (contexts.isEmpty()) {
                loadContext(contexts)
            }

            when (instruction) {
                is Action -> instruction.execute(actionExecutor, stateProvider, testContext)
                is Verification -> {

                    val result = verifyInstruction(instruction, contexts)

                    if (result.isFulfilled) {
                        successfulVerifications.add(instruction)
                    } else {
                        return TestResult(
                            testCase = testcase,
                            success = false,
                            message = result.failureMessage,
                            successfulVerifications = successfulVerifications.toList(),
                            failedVerification = instruction,
                            output = collectTestOutput()
                        )
                    }

                }
            }
        }

        return TestResult(
            testCase = testcase,
            success = true,
            message = "",
            successfulVerifications = successfulVerifications.toList(),
            output = collectTestOutput()
        )
    }

    private fun loadContext(contexts: MutableMap<String, ProcessInstanceKey>) {
        stateProvider.getProcessInstanceKeys()
            .mapIndexed { index, processInstanceKey ->
                contexts.put(
                    "process-$index",
                    processInstanceKey
                )
            }
    }

    private fun verifyInstruction(
        instruction: Verification,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult {
        val start = Instant.now()

        var result: VerificationResult
        do {
            result = instruction.verify(stateProvider, contexts)

            if (result.isFulfilled) {
                return result
            }

            val shouldRetry =
                Duration.between(start, Instant.now()).minus(verificationTimeout).isNegative

            if (shouldRetry) {
                Thread.sleep(verificationRetryInterval.toMillis())
            }
        } while (shouldRetry)

        return result
    }

    private fun collectTestOutput(): List<TestOutput> {
        return stateProvider.getProcessInstanceKeys().map { processInstanceKey ->
            TestOutput(
                processInstanceKey = processInstanceKey,
                state = stateProvider.getProcessInstanceState(processInstanceKey),
                elementInstances = stateProvider.getElementInstances(processInstanceKey),
                variables = stateProvider.getProcessInstanceVariables(processInstanceKey),
                incidents = stateProvider.getIncidents(processInstanceKey)
            )
        }
    }


}