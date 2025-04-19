package it.daniele.temporaryjobplacement.dtos.contact

import it.daniele.temporaryjobplacement.entities.contact.Address
import jakarta.validation.constraints.NotBlank

    data class AddressDTO(
        val id: Int,
        @field:NotBlank val address: String,
    )

fun Address.toDTO(): AddressDTO = AddressDTO(getId(), address)