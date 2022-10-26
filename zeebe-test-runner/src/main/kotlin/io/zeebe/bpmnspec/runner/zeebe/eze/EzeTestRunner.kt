package io.zeebe.bpmnspec.runner.zeebe.eze

import io.zeebe.bpmnspec.runner.zeebe.AbstractTestRunner
import io.zeebe.bpmnspec.runner.zeebe.ZeebeTestContext
import mu.KLogging

class EzeTestRunner(
    ezeEnvironment: EzeEnvironment = DefaultEzeTestEnvironment(),
    reuseEnvironment: Boolean = false,
    beforeEachCallback: (ZeebeTestContext) -> Unit = {},
    afterEachCallback: (ZeebeTestContext) -> Unit = {}
): AbstractTestRunner(environment = ezeEnvironment, reuseEnvironment, beforeEachCallback, afterEachCallback, logger) {

    companion object: KLogging()
}
