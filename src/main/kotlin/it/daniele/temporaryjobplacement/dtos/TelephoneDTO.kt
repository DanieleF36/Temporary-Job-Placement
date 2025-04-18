package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.contact.Telephone

data class TelephoneDTO(
    val prefix: String,
    val number: String
)

fun Telephone.toDTO(): TelephoneDTO = TelephoneDTO(prefix, number)
