package org.camunda.community.zeebe.spec.api

import org.camunda.community.zeebe.spec.ProcessInstanceKey
import org.camunda.community.zeebe.spec.runner.SpecStateProvider

interface Verification : Instruction {

    fun verify(
        stateProvider: SpecStateProvider,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult

}