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
    var name: String,
    var surname: String,
    @ManyToMany
    val email: MutableList<Email>,
    @ManyToMany
    val address: MutableList<Address>,
    @ManyToMany
    val telephone: MutableList<Telephone>,
    var ssn: String?,
    var category: Category
): EntityBase(){
    fun updatePhone(phoneId: Int, tel: Telephone){
        telephone.removeIf { it.getId() == phoneId }
        telephone.add(tel)
    }
}