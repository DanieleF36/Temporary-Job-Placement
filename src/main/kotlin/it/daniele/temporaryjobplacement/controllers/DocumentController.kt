package it.daniele.temporaryjobplacement.controllers

import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import it.daniele.temporaryjobplacement.services.DocumentService
import jakarta.validation.constraints.Min
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
        @RequestParam(defaultValue = "0") @Min(0, message = "Page number must be >= 0") page: Int,
        @RequestParam(defaultValue = "10") @Min(0, message = "Limit number must be > 0")limit: Int,
        @RequestParam sort: String?
    ): Page<DocumentMetadataDTO> {
        val allowedSort = listOf("name", "size", "creationTimestamp", "contentType")
        return documentService.getAll(page, limit, validateSort(allowedSort, sort, "name"))
    }

    @GetMapping("/{metadataId}")
    fun get(@PathVariable @Min(0, message = "Id must be >= 0") metadataId: Int): DocumentMetadataDTO {
        val meta = documentService.get(metadataId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "id not found")
        return meta
    }

    @GetMapping("/{metadataId}/data")
    fun getData(@PathVariable @Min(0, message = "Id must be >= 0") metadataId: Int): ResponseEntity<ByteArray> {
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
        @PathVariable @Min(0, message = "Id must be >= 0") metadataId: Int,
        @RequestParam("file", required = false) file: MultipartFile?,
        @RequestParam("name", required = false) name: String?,
        @RequestParam("contentType", required = false) contentType: String?
    ): DocumentMetadataDTO{
        if(name?.isBlank() == true)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must be not blank")
        return documentService.modify(metadataId, name, contentType, file?.bytes)
    }
    @DeleteMapping("/{metadataId}")
    fun delete(@PathVariable @Min(0, message = "Id must be >= 0") metadataId: Int){
        return documentService.delete(metadataId)
    }
}