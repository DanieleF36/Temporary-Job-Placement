package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class Telephone (
    @Column
    var prefix: String,
    @Column
    var number: String,
    @ManyToMany(mappedBy = "telephone")
    val contact: MutableList<Contact>
): EntityBase()