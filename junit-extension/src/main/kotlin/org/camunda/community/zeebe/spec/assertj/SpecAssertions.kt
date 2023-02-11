package org.camunda.community.zeebe.spec.assertj

import org.camunda.community.zeebe.spec.api.TestResult

object SpecAssertions {

    fun assertThat(actual: TestResult): SpecTestResultAssert =
        SpecTestResultAssert.assertThat(actual = actual)

}