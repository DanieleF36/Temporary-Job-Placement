package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import it.daniele.temporaryjobplacement.entities.message.Message
import jakarta.persistence.*

@Entity
class Contact(
    var name: String,
    var surname: String,
    @ManyToMany
    val email: MutableList<Email>,
    @ManyToMany
    val address: MutableList<Address>,
    @ManyToMany
    val telephone: MutableList<Telephone>,
    var ssn: String?,
    var category: Category,
    @OneToMany( fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], mappedBy = "sender", orphanRemoval = true)
    val messages: MutableList<Message> = mutableListOf()
): EntityBase()