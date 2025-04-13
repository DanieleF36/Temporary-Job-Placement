package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Email(
    @Column(unique = true, nullable = false)
    val email: String,
    @OneToMany(mappedBy = "email")
    val contact: List<Contact>
): EntityBase()