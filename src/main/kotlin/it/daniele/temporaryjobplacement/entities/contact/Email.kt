package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class Email(
    @Column(unique = true, nullable = false)
    var email: String,
    @ManyToMany
    val contact: MutableList<Contact>
): EntityBase()