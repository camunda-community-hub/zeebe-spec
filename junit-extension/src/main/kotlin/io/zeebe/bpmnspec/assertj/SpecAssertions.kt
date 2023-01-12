package io.zeebe.bpmnspec.assertj

import io.zeebe.bpmnspec.api.TestResult

object SpecAssertions {

    fun assertThat(actual: TestResult): SpecTestResultAssert =
        SpecTestResultAssert.assertThat(actual = actual)

}