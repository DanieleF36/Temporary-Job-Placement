package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.contact.Category
import it.daniele.temporaryjobplacement.entities.contact.Contact
import jakarta.validation.constraints.NotBlank


data class ContactDTO(
    val id: Int=0,
    @field:NotBlank val name: String,
    @field:NotBlank val surname: String,
    val email: List<@NotBlank String>,
    val address: List<@NotBlank  String>,
    val telephone: List<@NotBlank String>,
    @field:OptionalNotBlank val ssn: String?,
    val category: Category
)

fun Contact.toDTO(): ContactDTO =
    ContactDTO(this.getId(), name, surname, email.map { it.email }, address.map { it.address }, telephone .map { "${it.prefix}${it.number}" }, ssn, category)
