package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.dtos.DocumentBinaryDataDTO
import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface DocumentService {
    fun getAll(page: Int, limit: Int, sort: Sort): Page<DocumentMetadataDTO>
    fun get(id: Int): DocumentMetadataDTO?
    fun getData(id: Int): DocumentBinaryDataDTO?
    fun create(name: String, contentType: String, binaryContent: ByteArray): DocumentMetadataDTO
    fun modify(metadataId: Int, name: String?, contentType: String?, binaryContent: ByteArray?): DocumentMetadataDTO
    fun delete(id: Int)
}