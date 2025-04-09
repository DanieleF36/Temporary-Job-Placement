package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import java.time.ZonedDateTime

data class MessageDTO(
    val id: Int,
    val sender: ContactDTO,
    val date: ZonedDateTime,
    val subject: String?,
    val body: String?,
    val channel: Channel,
    val priority: Int,
    val state: State,
    val actions: List<ActionDTO>,
)

fun Message.toDTO(): MessageDTO=
    MessageDTO(this.getId(), sender.toDTO(), date, subject, body, channel, priority, state, actions.map { it.toDTO() })