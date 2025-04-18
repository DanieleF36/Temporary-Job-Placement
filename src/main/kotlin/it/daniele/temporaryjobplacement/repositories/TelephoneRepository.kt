package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.contact.Telephone
import org.springframework.data.jpa.repository.JpaRepository

interface TelephoneRepository: JpaRepository<Telephone, Int> {
    fun findByPrefixAndNumber(prefix: String, number: String): MutableList<Telephone>
    fun removeById(id: Int)
}