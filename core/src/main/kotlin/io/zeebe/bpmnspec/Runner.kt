package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.TestResult
import io.zeebe.bpmnspec.api.TestSpec

interface Runner {

    fun init()

    fun run(test: TestSpec): TestResult

}