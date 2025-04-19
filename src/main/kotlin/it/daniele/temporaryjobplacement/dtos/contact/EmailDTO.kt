package it.daniele.temporaryjobplacement.dtos.contact

import it.daniele.temporaryjobplacement.entities.contact.Email
import jakarta.validation.constraints.NotBlank

data class EmailDTO(
    val id: Int,
    @field:NotBlank val email: String,
)

fun Email.toDTO(): EmailDTO = EmailDTO(getId(), email)
