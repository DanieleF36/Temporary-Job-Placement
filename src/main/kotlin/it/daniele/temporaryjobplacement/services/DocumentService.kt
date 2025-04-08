package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.controllers.SortOption
import it.daniele.temporaryjobplacement.dtos.DocumentBinaryDataDTO
import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import org.springframework.data.domain.Page

interface DocumentService {
    fun getAll(page: Int, limit: Int, sort: SortOption): Page<DocumentMetadataDTO>
    fun get(id: Int): DocumentMetadataDTO?
    fun getData(id: Int): DocumentBinaryDataDTO?
    fun create(name: String, contentType: String, binaryContent: ByteArray): DocumentMetadataDTO
    fun modify(metadataId: Int, name: String?, contentType: String?, binaryContent: ByteArray?): DocumentMetadataDTO
    fun delete(id: Int)
}