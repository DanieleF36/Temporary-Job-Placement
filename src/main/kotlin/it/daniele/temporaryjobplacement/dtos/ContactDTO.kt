package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.contact.Category
import it.daniele.temporaryjobplacement.entities.contact.Contact
import jakarta.validation.constraints.NotBlank


data class ContactDTO(
    val id: Int,
    @field:NotBlank val name: String,
    @field:NotBlank val surname: String,
    @field:OptionalNotBlank val email: String?,
    @field:OptionalNotBlank val address: String?,
    @field:OptionalNotBlank val telephone: String?,
    @field:OptionalNotBlank val ssn: String?,
    val category: Category
)

fun Contact.toDTO(): ContactDTO =
    ContactDTO(this.getId(), name, surname, email?.email, address?.address, "${telephone?.prefix}${telephone?.number}", ssn, category)
