package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.*
import io.zeebe.bpmnspec.format.SpecDeserializer
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class SpecRunner(
        val testRunner: TestRunner,
        val resourceDirectory: Path = Path.of("."),
        val verificationTimeout: Duration = Duration.ofSeconds(10),
        val verificationRetryInterval: Duration = Duration.ofMillis(10)
) {

    val logger = LoggerFactory.getLogger(SpecRunner::class.java)

    val specDeserializer = SpecDeserializer()

    fun run(input: InputStream): TestSpecResult {

        logger.trace("Reading the spec")
        val spec = specDeserializer.readSpec(input)

        logger.debug("Running {} tests", spec.testCases.size)
        val testResults = spec.testCases.map { testcase ->

            testRunner.init()

            spec.resources.forEach { resourceName ->
                val resourceStream = resourceDirectory.resolve(resourceName).toFile().inputStream()
                testRunner.deployWorkflow(resourceName, resourceStream)
            }

            logger.debug("Run the test [name: '{}', description: '{}']", testcase.name, testcase.description)
            val result = runTestCase(testcase)

            logger.debug("Test finished [name: '{}', success: '{}', message: '{}']", testcase.name, result.success, result.message)

            testRunner.cleanUp()

            result
        }

        logger.debug("All tests finished [{}/{} passed]",
                testResults.filter { it.success }.count(),
                testResults.size)

        return TestSpecResult(
                spec = spec,
                testResults = testResults
        )
    }

    private fun runTestCase(testcase: TestCase): TestResult {

        val context = testcase.actions.map { it.execute(testRunner) }
                .filterNotNull()
                .first()

        val start = Instant.now()

        for (verification in testcase.verifications) {

            var result: VerificationResult
            do {
                result = verification.verify(testRunner, context)

                val shouldRetry = !result.isFulfilled &&
                        Duration.between(start, Instant.now()).minus(verificationTimeout).isNegative

                if (shouldRetry) {
                    Thread.sleep(verificationRetryInterval.toMillis())
                }
            } while (shouldRetry)

            if (!result.isFulfilled) {
                return TestResult(
                        testCase = testcase,
                        success = false,
                        message = result.failureMessage
                )
            }
        }

        return TestResult(
                testCase = testcase,
                success = true,
                message = ""
        )
    }


}