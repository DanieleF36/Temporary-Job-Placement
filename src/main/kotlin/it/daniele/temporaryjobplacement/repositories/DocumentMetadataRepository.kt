package it.daniele.temporaryjobplacement.repositories

import it.daniele.temporaryjobplacement.entities.DocumentMetadata
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentMetadataRepository: JpaRepository<DocumentMetadata, Int> {
    fun findByName(name: String): DocumentMetadata?
}