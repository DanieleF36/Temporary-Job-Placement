package it.daniele.temporaryjobplacement.dtos.message

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.time.ZonedDateTime

data class MessageDTO(
    val id: Int = 0,
    @field:Positive val senderId: Int,
    val date: ZonedDateTime,
    val state: State,
    @field:OptionalNotBlank val subject: String?,
    @field:OptionalNotBlank val body: String?,
    @field:Min(0) val priority: Int = 0,
    val channel: Channel
)

fun Message.toDTO(): MessageDTO =
    MessageDTO(this.getId(), sender.getId(), date, state, subject, body, priority, channel)