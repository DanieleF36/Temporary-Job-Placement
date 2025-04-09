package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.contact.Contact
import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository: JpaRepository<Contact, Int> {
}