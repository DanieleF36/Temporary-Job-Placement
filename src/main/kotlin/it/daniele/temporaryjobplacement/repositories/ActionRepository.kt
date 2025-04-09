package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.message.Action
import org.springframework.data.jpa.repository.JpaRepository

interface ActionRepository: JpaRepository<Action, Int> {
}