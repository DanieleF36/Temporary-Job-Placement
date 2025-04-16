package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.contact.Telephone

data class TelephoneDTO(
    val prefix: Int,
    val number: Int
)

fun Telephone.toDTO(): TelephoneDTO = TelephoneDTO(prefix, number)
