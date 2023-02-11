package io.zeebe.bpmnspec.api

import io.zeebe.bpmnspec.ProcessInstanceKey
import io.zeebe.bpmnspec.runner.SpecStateProvider

interface Verification : Instruction {

    fun verify(
        stateProvider: SpecStateProvider,
        contexts: Map<String, ProcessInstanceKey>
    ): VerificationResult

}