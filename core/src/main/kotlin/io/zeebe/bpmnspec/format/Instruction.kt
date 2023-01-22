package io.zeebe.bpmnspec.format

data class Instruction(
    val action: String?,
    val verification: String?,
    val args: Map<String, String>?
) {

    fun type(): InstructionType {
        return action?.let { InstructionType.ACTION }
            ?: verification?.let { InstructionType.VERIFICATION }
            ?: throw IllegalArgumentException("The instruction should be either an action or a verification.")
    }

    fun toAction(): Action {
        return Action(
            action = action!!,
            args = args
        )
    }

    fun toVerification(): Verification {
        return Verification(
            verification = verification!!,
            args = args
        )
    }

    enum class InstructionType {
        ACTION,
        VERIFICATION
    }

}