package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.dtos.document.DocumentBinaryDataDTO
import it.daniele.temporaryjobplacement.dtos.document.DocumentMetadataDTO
import it.daniele.temporaryjobplacement.dtos.document.toDto
import it.daniele.temporaryjobplacement.entities.DocumentBinaryData
import it.daniele.temporaryjobplacement.entities.DocumentMetadata
import it.daniele.temporaryjobplacement.exceptions.DocumentNameAlreadyExists
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.repositories.DocumentBinaryRepository
import it.daniele.temporaryjobplacement.repositories.DocumentMetadataRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.data.domain.Sort
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
@Transactional
class DocumentServiceImpl(private val repoMeta: DocumentMetadataRepository, private val repoBinary: DocumentBinaryRepository): DocumentService {
    private val logger: Logger = LoggerFactory.getLogger(DocumentService::class.java)

    override fun getAll(page: Int, limit: Int, sort: Sort): Page<DocumentMetadataDTO> {
        if (page < 0) throw IllegalArgumentException("Page must be >= 0")
        if (limit <= 0) throw IllegalArgumentException("Limit must be > 0")

        val pageable = PageRequest.of(page, limit, sort)

        return repoMeta.findAll(pageable).map { it.toDto() }
    }

    override fun get(id: Int): DocumentMetadataDTO? {
        if(id < 0) throw IllegalArgumentException("ID must be >= 0")
        return repoMeta.findById(id).getOrNull()?.toDto()
    }

    override fun getData(id: Int): DocumentBinaryDataDTO? {
        return repoBinary.findById(id).getOrNull()?.toDto()
    }

    override fun create(name: String, contentType: String, binaryContent: ByteArray): DocumentMetadataDTO {
        if(name.isBlank()) throw IllegalArgumentException("Name must be not blank")
        if(contentType.isBlank()) throw IllegalArgumentException("ContentType must be not blank")
        if(repoMeta.findByName(name) != null)
            throw DocumentNameAlreadyExists("Duplicate name already exists")

        val meta = DocumentMetadata(
            name = name,
            size = binaryContent.size,
            contentType = contentType,
            creationTimestamp = ZonedDateTime.now()
        )

        val content = DocumentBinaryData(
            content = binaryContent,
            metadata = meta
        )
        meta.binaryContent=content
        repoMeta.save(meta)
        logger.info("Creato nuovo documento: $name")
        return meta.toDto()
    }

    override fun modify(metadataId: Int, name: String?, contentType: String?, binaryContent: ByteArray?): DocumentMetadataDTO {
        if(metadataId < 0) throw IllegalArgumentException("ID must be >= 0")
        if(name?.isBlank() == true) throw IllegalArgumentException("Name must be not blank")
        if(contentType?.isBlank() == true) throw IllegalArgumentException("ContentType must be not blank")

        val meta = repoMeta.findById(metadataId).getOrNull()
            ?: throw NotFoundException("File not found: $metadataId")
        meta.name = name ?: meta.name
        meta.size = binaryContent?.size ?: meta.size
        meta.contentType = contentType ?: meta.contentType

        if(binaryContent != null)
            meta.binaryContent!!.content = binaryContent
        logger.info("Documento modificato: $metadataId")
        return meta.toDto()
    }

    override fun delete(id: Int) {
        if(id < 0) throw IllegalArgumentException("ID must be >= 0")
        if(repoMeta.findById(id).isEmpty)
            throw NotFoundException("File not found: $id")
        repoMeta.deleteById(id)
        logger.info("Documento eliminato: $id")
    }
}