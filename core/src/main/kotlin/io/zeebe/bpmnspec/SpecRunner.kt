package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.*
import io.zeebe.bpmnspec.api.runner.TestRunner
import io.zeebe.bpmnspec.format.SpecDeserializer
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class SpecRunner(
        val testRunner: TestRunner,
        val resourceResolver: ResourceResolver = DirectoryResourceResolver(rootDirectory = Path.of(".")),
        val verificationTimeout: Duration = Duration.ofSeconds(10),
        val verificationRetryInterval: Duration = Duration.ofMillis(10)
) {

    private val logger = LoggerFactory.getLogger(SpecRunner::class.java)

    private val specDeserializer = SpecDeserializer()

    fun run(input: InputStream): TestSpecResult {

        logger.trace("Reading the spec")
        val spec = specDeserializer.readSpec(input)

        logger.debug("Running {} tests", spec.testCases.size)
        testRunner.beforeAll()

        val testResults = spec.testCases.map {
            runTestCase(
                    resources = spec.resources,
                    testcase = it)
        }

        testRunner.afterAll()

        logger.debug("All tests finished [{}/{} passed]",
                testResults.filter { it.success }.count(),
                testResults.size)

        return TestSpecResult(
                spec = spec,
                testResults = testResults
        )
    }

    fun runTestCase(resources: List<String>, testcase: TestCase): TestResult {
        logger.debug("Preparing the test [name: '{}']", testcase.name)
        testRunner.beforeEach()

        logger.debug("Deploying resources for the test. [name: '{}', resources: {}]", testcase.name, resources.joinToString())
        resources.forEach { resourceName ->
            val resourceStream = resourceResolver.getResource(resourceName)
            testRunner.deployWorkflow(resourceName, resourceStream)
        }

        logger.debug("Run the test [name: '{}', description: '{}']", testcase.name, testcase.description)
        val result = runTestCase(testcase)

        logger.debug("Test finished [name: '{}', success: '{}', message: '{}']", testcase.name, result.success, result.message)

        testRunner.afterEach()

        return result
    }

    private fun runTestCase(testcase: TestCase): TestResult {

        val contexts = mutableMapOf<String, WorkflowInstanceContext>()

        testcase.actions.forEach { it.execute(testRunner, contexts) }

        if (contexts.isEmpty()) {
            contexts.putAll(testRunner.getWorkflowInstanceContexts().map { Pair("unnamed", it) }.toMap())
        }

        val start = Instant.now()

        val successfulVerifications = mutableListOf<Verification>()

        for (verification in testcase.verifications) {

            var result: VerificationResult
            do {
                result = verification.verify(testRunner, contexts)

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
                        failedVerification = verification
                )
            }
        }

        return TestResult(
                testCase = testcase,
                success = true,
                message = "",
                successfulVerifications = successfulVerifications.toList()
        )
    }


}