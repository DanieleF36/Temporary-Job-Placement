package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.entities.DocumentMetadata
import java.time.ZonedDateTime

data class DocumentMetadataDTO(
    val id: Int?,
    val name: String,
    val size: Int,
    val contentType: String,
    val creationTimestamp: ZonedDateTime
)

fun DocumentMetadata.toDto(): DocumentMetadataDTO =
    DocumentMetadataDTO(this.id, this.name, this.size, this.contentType, this.creationTimestamp)