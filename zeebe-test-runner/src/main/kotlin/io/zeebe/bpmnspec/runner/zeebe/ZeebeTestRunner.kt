package io.zeebe.bpmnspec.runner.zeebe

import mu.KLogging

class ZeebeTestRunner(
    environment: ZeebeEnvironment = DefaultZeebeEnvironment(),
    reuseEnvironment: Boolean = false,
    beforeEachCallback: (ZeebeTestContext) -> Unit = {},
    afterEachCallback: (ZeebeTestContext) -> Unit = {}
) : AbstractTestRunner(environment, reuseEnvironment, beforeEachCallback, afterEachCallback, logger) {

    companion object: KLogging()

}
