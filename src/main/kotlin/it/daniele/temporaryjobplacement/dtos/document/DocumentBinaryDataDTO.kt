package it.daniele.temporaryjobplacement.dtos.document

import it.daniele.temporaryjobplacement.entities.DocumentBinaryData

data class DocumentBinaryDataDTO(
    val id: Int = 0,
    val content: ByteArray,
    var metadataDTO: DocumentMetadataDTO
)

fun DocumentBinaryData.toDto(): DocumentBinaryDataDTO =
        DocumentBinaryDataDTO(this.getId(), this.content, this.metadata.toDto())