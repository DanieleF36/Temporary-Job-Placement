package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Telephone (
    val prefix: Int,
    val number: Int,
    @OneToMany
    val contact: List<Contact>
): EntityBase()