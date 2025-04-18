package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany


@Entity
class Address(
    @Column(unique = true, nullable = false)
    var address: String,
    @ManyToMany(mappedBy = "address")
    val contact: MutableList<Contact>
): EntityBase()
