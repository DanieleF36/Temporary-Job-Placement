package it.daniele.temporaryjobplacement.dtos.contact

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.contact.Category
import it.daniele.temporaryjobplacement.entities.contact.Contact
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ContactDTO(
    val id: Int = 0,
    @field:NotBlank val name: String,
    @field:NotBlank val surname: String,
    @field:Valid val email: List<EmailDTO>,
    @field:Valid val address: List<AddressDTO>,
    @field:Valid val telephone: List<TelephoneDTO>,
    @field:OptionalNotBlank val ssn: String?,
    val category: Category
)

fun Contact.toDTO(): ContactDTO =
    ContactDTO(this.getId(), name, surname, email.map { it.toDTO() }, address.map { it.toDTO() }, telephone.map { it.toDTO() }, ssn, category)
