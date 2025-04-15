package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.contact.Email
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository: JpaRepository<Email, Int> {
    fun findByEmail(email: String): MutableList<Email>
}