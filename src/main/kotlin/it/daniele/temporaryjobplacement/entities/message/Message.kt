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
    var subject: String?,
    var body: String?,
    val channel: Channel,
    var priority: Int,
    var state: State,
    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val actions: MutableList<Action>,
): EntityBase()