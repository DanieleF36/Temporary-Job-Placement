package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.time.ZonedDateTime

data class MessageDTO(
    val id: Int = 0,
    @Valid val sender: ContactDTO,
    val date: ZonedDateTime,
    val channel: Channel,
    val state: State,
    val actions: List<ActionDTO>,
    @field:OptionalNotBlank val subject: String?,
    @field:OptionalNotBlank val body: String?,
    @field:Min(0) val priority: Int,
)

fun Message.toDTO(): MessageDTO=
    MessageDTO(this.getId(), sender.toDTO(), date, subject, body, channel, priority, state, actions.map { it.toDTO() })