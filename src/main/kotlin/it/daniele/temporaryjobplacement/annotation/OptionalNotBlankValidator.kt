package it.daniele.temporaryjobplacement.annotation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class OptionalNotBlankValidator : ConstraintValidator<OptionalNotBlank, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || value.trim().isNotEmpty()
    }
}