package it.daniele.temporaryjobplacement.annotation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NotBlankElementsValidator : ConstraintValidator<NotBlankElements, List<String>> {
    override fun isValid(value: List<String>?, context: ConstraintValidatorContext) =
        value == null || value.all { it.isNotBlank() }
}