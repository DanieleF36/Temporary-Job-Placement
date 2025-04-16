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
    val email: MutableList<Email>,
    @ManyToMany(mappedBy = "contact")
    val address: MutableList<Address>,
    @ManyToMany(mappedBy = "contact")
    val telephone: MutableList<Telephone>,
    val ssn: String?,
    var category: Category
): EntityBase(){
    fun updatePhone(phoneId: Int, tel: Telephone){
        telephone.removeIf { it.getId() == phoneId }
        telephone.add(tel)
    }
}