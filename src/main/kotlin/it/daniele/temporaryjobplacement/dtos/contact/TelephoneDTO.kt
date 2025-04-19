package it.daniele.temporaryjobplacement.dtos.contact

import it.daniele.temporaryjobplacement.entities.contact.Telephone
import jakarta.validation.constraints.NotBlank

data class TelephoneDTO(
    val id: Int = 0,
    @field:NotBlank val prefix: String,
    @field:NotBlank val number: String
)

fun Telephone.toDTO(): TelephoneDTO = TelephoneDTO(getId(), prefix, number)
