package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

@Entity
class Contact(
    val name: String,
    val surname: String,
    @ManyToMany(mappedBy = "contact")
    val email: List<Email>,
    @ManyToMany(mappedBy = "contact")
    val address: List<Address>,
    @ManyToMany(mappedBy = "contact")
    val telephone: List<Telephone>,
    val ssn: String?,
    val category: Category
): EntityBase()