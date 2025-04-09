package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.message.Action
import it.daniele.temporaryjobplacement.entities.message.State
import java.time.ZonedDateTime

data class ActionDTO(
    val id: Int,
    val state: State,
    val date: ZonedDateTime,
    val comment: String?,
)

fun Action.toDTO(): ActionDTO=
        ActionDTO(this.getId(), this.state, this.date, this.comment)
