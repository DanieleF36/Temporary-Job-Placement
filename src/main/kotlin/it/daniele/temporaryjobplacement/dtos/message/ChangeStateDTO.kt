package it.daniele.temporaryjobplacement.dtos.message

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.message.State

data class ChangeStateDTO(
    val newState: State,
    @field:OptionalNotBlank val comment: String?
)
