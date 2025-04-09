package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository: JpaRepository<Message, Int> {
    fun findByState(state: State, pageable: Pageable): Page<Message>
}