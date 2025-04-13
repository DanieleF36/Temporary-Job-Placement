package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Telephone (
    @Column
    val prefix: Int,
    @Column
    val number: Int,
    @OneToMany(mappedBy = "telephone")
    val contact: List<Contact>
): EntityBase()