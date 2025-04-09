package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.contact.Category
import it.daniele.temporaryjobplacement.entities.contact.Contact


data class ContactDTO(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String?,
    val address: String?,
    val telephone: String?,
    val ssn: String?,
    val category: Category
)

fun Contact.toDTO(): ContactDTO =
    ContactDTO(this.getId(), name, surname, email?.email, address?.address, "${telephone?.prefix}${telephone?.number}", ssn, category)
