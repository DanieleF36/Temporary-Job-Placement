package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.contact.Contact
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ContactRepository : JpaRepository<Contact, Int> {
    // Single field queries
    fun findByEmailContainsIgnoreCase(email: String, pageable: Pageable): Page<Contact>
    fun findByTelephoneContainsIgnoreCase(telephone: String, pageable: Pageable): Page<Contact>
    fun findByNameContainsIgnoreCase(name: String, pageable: Pageable): Page<Contact>
    fun findBySurnameContainsIgnoreCase(surname: String, pageable: Pageable): Page<Contact>

    // Queries with two parameters (specific cases)
    fun findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCase(name: String, surname: String, pageable: Pageable): Page<Contact>
    fun findByEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(email: String, telephone: String, pageable: Pageable): Page<Contact>

    // Queries with two mixed field combinations not yet covered
    fun findByNameContainsIgnoreCaseOrEmailContainsIgnoreCase(name: String, email: String, pageable: Pageable): Page<Contact>
    fun findByNameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name: String, telephone: String, pageable: Pageable): Page<Contact>
    fun findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCase(surname: String, email: String, pageable: Pageable): Page<Contact>
    fun findBySurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(surname: String, telephone: String, pageable: Pageable): Page<Contact>

    // Queries with three parameters for common use cases
    fun findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrEmailContainsIgnoreCase(name: String, surname: String, email: String, pageable: Pageable): Page<Contact>
    fun findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name: String, surname: String, telephone: String, pageable: Pageable): Page<Contact>

    // Queries with three mixed field combinations not covered above
    fun findByNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name: String, email: String, telephone: String, pageable: Pageable): Page<Contact>
    fun findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(surname: String, email: String, telephone: String, pageable: Pageable): Page<Contact>

    // Query with all four parameters
    fun findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCaseOrEmailContainsIgnoreCase(
        name: String, surname: String, telephone: String, email: String, pageable: Pageable
    ): Page<Contact>
}