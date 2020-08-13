package io.zeebe.bpmnspec

import io.zeebe.bpmnspec.api.TestResult
import io.zeebe.bpmnspec.api.TestCase

interface Runner {

    fun init()

    fun run(test: TestCase): TestResult

}