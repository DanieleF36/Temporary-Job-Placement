package it.daniele.temporaryjobplacement.entities.message

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
class Action(
    @ManyToOne
    val message: Message? = null,
    val state: State,
    val date: ZonedDateTime,
    val comment: String?,
): EntityBase()