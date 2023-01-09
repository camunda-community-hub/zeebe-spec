package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.*
import io.zeebe.bpmnspec.format.SpecDeserializer
import io.zeebe.bpmnspec.runner.SpecActionExecutor
import io.zeebe.bpmnspec.runner.SpecStateProvider
import io.zeebe.bpmnspec.runner.TestRunnerEnvironment
import io.zeebe.bpmnspec.runner.eze.EzeEnvironment
import org.slf4j.LoggerFactory
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

        val testResults = spec.testCases.map {
            runTestCase(
                resources = spec.resources,
                testcase = it
            )
        }

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

    fun runSingleTestCase(resources: List<String>, testcase: TestCase): TestResult {
        logger.debug("Run a single test")

        val testResult = runTestCase(
            resources = resources,
            testcase = testcase
        )

        return testResult
    }

    private fun runTestCase(resources: List<String>, testcase: TestCase): TestResult {
        logger.debug("Prepare the test [name: '{}']", testcase.name)

        logger.debug("Create spec test environment")
        environment.create()

        logger.debug("Invoke before-each callback")
        beforeEachCallback(environment.getContext())

        logger.debug(
            "Deploy resources for the test. [name: '{}', resources: {}]",
            testcase.name,
            resources.joinToString()
        )
        resources.forEach { resourceName ->
            val resourceStream = resourceResolver.getResource(resourceName)
            actionExecutor.deployProcess(resourceName, resourceStream)
        }

        logger.debug(
            "Run the test [name: '{}', description: '{}']",
            testcase.name,
            testcase.description
        )
        val result = runTestCase(testcase)

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

    private fun runTestCase(testcase: TestCase): TestResult {

        val contexts = mutableMapOf<String, ProcessInstanceKey>()

        // TODO (saig0): handle case when the context is not found
        val testContext = TestContext(
            storeContext = { alias, context -> contexts.put(alias, context) },
            getContext = { alias -> alias.let { contexts[it] } ?: contexts.values.first() },
            verificationTimeout = verificationTimeout,
            verificationRetryInterval = verificationRetryInterval
        )

        testcase.actions.forEach { it.execute(actionExecutor, stateProvider, testContext) }

        if (contexts.isEmpty()) {
            stateProvider.getProcessInstanceKeys()
                .mapIndexed { index, processInstanceKey ->
                    contexts.put(
                        "process-$index",
                        processInstanceKey
                    )
                }
        }

        val start = Instant.now()

        val successfulVerifications = mutableListOf<Verification>()

        for (verification in testcase.verifications) {

            var result: VerificationResult
            do {
                result = verification.verify(stateProvider, contexts)

                val shouldRetry = !result.isFulfilled &&
                        Duration.between(start, Instant.now()).minus(verificationTimeout).isNegative

                if (shouldRetry) {
                    Thread.sleep(verificationRetryInterval.toMillis())
                }
            } while (shouldRetry)

            if (result.isFulfilled) {
                successfulVerifications.add(verification)
            } else {
                return TestResult(
                    testCase = testcase,
                    success = false,
                    message = result.failureMessage,
                    successfulVerifications = successfulVerifications.toList(),
                    failedVerification = verification,
                    output = collectTestOutput()
                )
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