package it.daniele.temporaryjobplacement.controllers

import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import it.daniele.temporaryjobplacement.services.DocumentService
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/API/documents")
class DocumentController(private val documentService: DocumentService) {
    @GetMapping
    fun getAll(
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "NAME_ASC") sort: SortOption
    ): Page<DocumentMetadataDTO> {
        if(page < 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number must be >= 0")
        if(limit <= 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit number must be > 0")
        return documentService.getAll(page, limit, sort)
    }

    @GetMapping("/{metadataId}")
    fun get(@PathVariable metadataId: Int): DocumentMetadataDTO {
        if(metadataId < 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be >= 0")
        val meta = documentService.get(metadataId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "id not found")
        return meta
    }

    @GetMapping("/{metadataId}/data")
    fun getData(@PathVariable metadataId: Int): ResponseEntity<ByteArray> {
        if(metadataId < 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be >= 0")
        val data = documentService.getData(metadataId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "id not found")
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(data.metadataDTO.contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${data.metadataDTO.name}\"")
            .body(data.content)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createDocument(@RequestParam("file") file: MultipartFile): DocumentMetadataDTO {
        val name = file.originalFilename ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome file mancante")
        val contentType = file.contentType ?: "application/octet-stream"
        val content = file.bytes

        return documentService.create(name, contentType, content)
    }

    @PutMapping("/{metadataId}")
    fun modify(
        @PathVariable metadataId: Int,
        @RequestParam("file", required = false) file: MultipartFile?,
        @RequestParam("name", required = false) name: String?,
        @RequestParam("contentType", required = false) contentType: String?
    ): DocumentMetadataDTO{
        if(metadataId < 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be >= 0")
        if(name?.isBlank() == true)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must be not blank")
        return documentService.modify(metadataId, name, contentType, file?.bytes)
    }
    @DeleteMapping("/{metadataId}")
    fun delete(@PathVariable metadataId: Int){
        if(metadataId < 0)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be >= 0")
        return documentService.delete(metadataId)
    }
}