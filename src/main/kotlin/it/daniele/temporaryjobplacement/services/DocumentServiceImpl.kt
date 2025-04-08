package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.controllers.SortOption
import it.daniele.temporaryjobplacement.dtos.DocumentBinaryDataDTO
import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import it.daniele.temporaryjobplacement.dtos.toDto
import it.daniele.temporaryjobplacement.entities.DocumentBinaryData
import it.daniele.temporaryjobplacement.entities.DocumentMetadata
import it.daniele.temporaryjobplacement.exceptions.DocumentNameAlreadyExists
import it.daniele.temporaryjobplacement.exceptions.DocumentNotFoundException
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

    override fun getAll(page: Int, limit: Int, sort: SortOption): Page<DocumentMetadataDTO> {
        if (page < 0) throw IllegalArgumentException("Page must be >= 0")
        if (limit <= 0) throw IllegalArgumentException("Limit must be > 0")

        val s = when (sort) {
            SortOption.NAME_ASC -> Sort.by(Sort.Direction.ASC, "name")
            SortOption.NAME_DESC -> Sort.by(Sort.Direction.DESC, "name")
            SortOption.SIZE_ASC -> Sort.by(Sort.Direction.ASC, "size")
            SortOption.SIZE_DESC -> Sort.by(Sort.Direction.DESC, "size")
            SortOption.CREATION_DATE_ASC -> Sort.by(Sort.Direction.ASC, "creationTimestamp")
            SortOption.CREATION_DATE_DESC -> Sort.by(Sort.Direction.DESC, "creationTimestamp")
            SortOption.CONTENT_TYPE_ASC -> Sort.by(Sort.Direction.ASC, "contentType")
            SortOption.CONTENT_TYPE_DESC -> Sort.by(Sort.Direction.DESC, "contentType")
        }
        val pageable = PageRequest.of(page, limit, s)

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
            ?: throw DocumentNotFoundException("File not found: $metadataId")
        val modifiedMeta = DocumentMetadata(
            id = meta.id,
            name = name ?: meta.name,
            size = binaryContent?.size ?: meta.size,
            contentType = contentType ?: meta.contentType,
            creationTimestamp = meta.creationTimestamp,
        )
        val content = DocumentBinaryData(
            content = binaryContent ?: meta.binaryContent!!.content,
            metadata = modifiedMeta
        )
        modifiedMeta.binaryContent = content
        repoMeta.save(modifiedMeta)
        logger.info("Documento modificato: $metadataId")
        return modifiedMeta.toDto()
    }

    override fun delete(id: Int) {
        if(id < 0) throw IllegalArgumentException("ID must be >= 0")
        if(repoMeta.findById(id).isEmpty)
            throw DocumentNotFoundException("File not found: $id")
        repoMeta.deleteById(id)
        logger.info("Documento eliminato: $id")
    }
}