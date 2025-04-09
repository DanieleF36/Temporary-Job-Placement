package it.daniele.temporaryjobplacement.entities.contact

import it.daniele.temporaryjobplacement.entities.EntityBase
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Contact(
    val name: String,
    val surname: String,
    @ManyToOne
    val email: Email?,
    @ManyToOne
    val address: Address?,
    @ManyToOne
    val telephone: Telephone?,
    val ssn: String?,
    val category: Category
): EntityBase()