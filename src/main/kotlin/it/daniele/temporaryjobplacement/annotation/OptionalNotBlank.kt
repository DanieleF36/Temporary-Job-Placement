package it.daniele.temporaryjobplacement.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OptionalNotBlank(
    val message: String = "If present, the field must be not blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)
