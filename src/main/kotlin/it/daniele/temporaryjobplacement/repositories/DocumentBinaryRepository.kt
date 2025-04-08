package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.DocumentBinaryData
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentBinaryRepository: JpaRepository<DocumentBinaryData, Int> {
}