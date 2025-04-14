package it.daniele.temporaryjobplacement.dtos.message

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.entities.message.Channel
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.time.ZonedDateTime

data class CreateMessageDTO(
    @field:Positive val senderId: Int,
    val date: ZonedDateTime,
    @field:OptionalNotBlank val subject: String?,
    @field:OptionalNotBlank val body: String?,
    @field:Min(0) val priority: Int = 0,
    val channel: Channel
)