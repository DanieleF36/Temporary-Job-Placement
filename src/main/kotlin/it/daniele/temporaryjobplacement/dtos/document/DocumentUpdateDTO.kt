package it.daniele.temporaryjobplacement.dtos.document

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import org.springframework.web.multipart.MultipartFile

data class DocumentUpdateDTO(
    @field:OptionalNotBlank val name: String?,
    @field:OptionalNotBlank  val contentType: String?,
    val file: MultipartFile?
)