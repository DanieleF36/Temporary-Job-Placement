package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany


@Entity
class Address(
    @Column(unique = true, nullable = false)
    var address: String,
    @OneToMany
    val contact: List<Contact>
): EntityBase()
