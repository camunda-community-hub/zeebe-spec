package io.zeebe.bpmnspec.junit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BpmnSpec(val specResource: String) {
}