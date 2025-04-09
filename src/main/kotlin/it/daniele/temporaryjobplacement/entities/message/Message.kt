package it.daniele.temporaryjobplacement.entities.message

import it.daniele.temporaryjobplacement.entities.contact.Contact
import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
class Message(
    @ManyToOne
    val sender: Contact,
    val date: ZonedDateTime,
    val subject: String?,
    val body: String?,
    val channel: Channel,
    val priority: Int,
    val state: State,
    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    val actions: List<Action>,
): EntityBase()