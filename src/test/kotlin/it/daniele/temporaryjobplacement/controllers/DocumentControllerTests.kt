package it.daniele.temporaryjobplacement.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.daniele.temporaryjobplacement.dtos.DocumentBinaryDataDTO
import it.daniele.temporaryjobplacement.dtos.DocumentMetadataDTO
import it.daniele.temporaryjobplacement.services.DocumentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime

internal class DocumentControllerTests {
    private val documentService: DocumentService = mockk()
    private val controller = DocumentController(documentService)

    /** --------------------getAll----------------------- **/
    @Test
    fun `getAll throws ResponseStatusException when page is negative`() {
        val page = -1
        val limit = 10
        val sort = SortOption.NAME_ASC

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getAll(page, limit, sort)
        }
        assertEquals("Page number must be >= 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `getAll throws ResponseStatusException when limit is less or equal to zero`() {
        val page = 0
        val limit = 0
        val sort = SortOption.NAME_ASC

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getAll(page, limit, sort)
        }
        assertEquals("Limit number must be > 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `getAll returns page`() {
        val page = 0
        val limit = 10
        val sort = SortOption.NAME_ASC
        val fakePage: Page<DocumentMetadataDTO> = PageImpl(emptyList())
        every { documentService.getAll(page, limit, sort) } returns fakePage

        val result = controller.getAll(page, limit, sort)

        assertEquals(fakePage, result)
        verify(exactly = 1) { documentService.getAll(page, limit, sort) }
    }

    /** --------------------get----------------------- **/
    @Test
    fun `get throws ResponseStatusException when metadataId is negative`(){
        val metadataId = -1

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.get(metadataId)
        }
        assertEquals("id must be >= 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `get throws ResponseStatusException when metadataId is not found`(){
        val metadataId = 1

        every { documentService.get(metadataId) } returns null

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.get(metadataId)
        }
        assertEquals("id not found", exception.reason)
        assertEquals(404, exception.statusCode.value())
    }

    @Test
    fun `get returns document`() {
        val metadataId = 1
        val fakeMetadata = DocumentMetadataDTO(
            id = 1,
            name = "name",
            size = 10,
            contentType = "content-type",
            creationTimestamp = ZonedDateTime.now(),
        )
        every { documentService.get(metadataId) } returns fakeMetadata

        val result = controller.get(metadataId)

        assertEquals(fakeMetadata, result)
        verify(exactly = 1) { documentService.get(metadataId) }
    }

    /** --------------------getData----------------------- **/
    @Test
    fun `getData throws ResponseStatusException when metadataId is negative`() {
        val metadataId = -1

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getData(metadataId)
        }
        assertEquals("id must be >= 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `getData throws ResponseStatusException when document data is not found`() {
        val metadataId = 1
        every { documentService.getData(metadataId) } returns null

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getData(metadataId)
        }
        assertEquals("id not found", exception.reason)
        assertEquals(404, exception.statusCode.value())
    }

    @Test
    fun `getData returns document binary data with correct headers`() {
        val metadataId = 1
        val fakeMetadata = DocumentMetadataDTO(
            id = metadataId,
            name = "file.pdf",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        val fakeContent = "binary data".toByteArray()
        val fakeDataDTO = DocumentBinaryDataDTO(
            content = fakeContent,
            metadataDTO = fakeMetadata
        )
        every { documentService.getData(metadataId) } returns fakeDataDTO

        val response = controller.getData(metadataId)

        assertEquals(200, response.statusCode.value())
        assertEquals("application/pdf", response.headers.contentType!!.toString())
        val expectedContentDisposition = "attachment; filename=\"${fakeMetadata.name}\""
        assertEquals(expectedContentDisposition, response.headers.getFirst("Content-Disposition"))
        assertEquals(fakeContent, response.body)
    }

    /** --------------------createDocument----------------------- **/
    @Test
    fun `createDocument throws exception when originalFilename is null`() {
        val file: MultipartFile = mockk()
        every { file.originalFilename } returns null

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.createDocument(file)
        }
        assertEquals("Nome file mancante", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `createDocument uses default content type when contentType is null`() {
        val file: MultipartFile = mockk()
        val fileName = "test.pdf"
        val defaultContentType = "application/octet-stream"
        val fileBytes = "dummy content".toByteArray()

        every { file.originalFilename } returns fileName
        every { file.contentType } returns null
        every { file.bytes } returns fileBytes

        val fakeMetadata = DocumentMetadataDTO(
            id = 1,
            name = fileName,
            size = fileBytes.size,
            contentType = defaultContentType,
            creationTimestamp = ZonedDateTime.now()
        )

        every { documentService.create(fileName, defaultContentType, fileBytes) } returns fakeMetadata

        val result = controller.createDocument(file)

        assertEquals(fakeMetadata, result)
        verify(exactly = 1) { documentService.create(fileName, defaultContentType, fileBytes) }
    }

    @Test
    fun `createDocument returns document when file is valid`() {
        val file: MultipartFile = mockk()
        val fileName = "test.pdf"
        val fileContentType = "application/pdf"
        val fileBytes = "dummy content".toByteArray()

        every { file.originalFilename } returns fileName
        every { file.contentType } returns fileContentType
        every { file.bytes } returns fileBytes

        val fakeMetadata = DocumentMetadataDTO(
            id = 1,
            name = fileName,
            size = fileBytes.size,
            contentType = fileContentType,
            creationTimestamp = ZonedDateTime.now()
        )
        every { documentService.create(fileName, fileContentType, fileBytes) } returns fakeMetadata

        val result = controller.createDocument(file)

        assertEquals(fakeMetadata, result)
        verify(exactly = 1) { documentService.create(fileName, fileContentType, fileBytes) }
    }

    /** --------------------modify----------------------- **/
    @Test
    fun `modify throws ResponseStatusException when metadataId is negative`() {
        val metadataId = -1
        val file: MultipartFile = mockk()
        every { file.bytes } returns "dummy".toByteArray()

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.modify(metadataId, file, "newName", "application/pdf")
        }
        assertEquals("id must be >= 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `modify throws ResponseStatusException when name is blank`() {
        val metadataId = 1
        val file: MultipartFile = mockk()
        every { file.bytes } returns "dummy".toByteArray()

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.modify(metadataId, file, "   ", "application/pdf")
        }
        assertEquals("name must be not blank", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `modify returns document when name is null and valid parameters are provided`() {
        val metadataId = 1
        val file: MultipartFile = mockk()
        val fileBytes = "updated content".toByteArray()
        val contentType = "application/pdf"
        every { file.bytes } returns fileBytes

        val expectedDto = DocumentMetadataDTO(
            id = metadataId,
            name = "defaultName",
            size = fileBytes.size,
            contentType = contentType,
            creationTimestamp = ZonedDateTime.now()
        )
        every { documentService.modify(metadataId, null, contentType, fileBytes) } returns expectedDto

        val result = controller.modify(metadataId, file, null, contentType)

        assertEquals(expectedDto, result)
        verify(exactly = 1) { documentService.modify(metadataId, null, contentType, fileBytes) }
    }


    @Test
    fun `modify returns document when valid parameters are provided`() {
        val metadataId = 1
        val file: MultipartFile = mockk()
        val fileBytes = "updated content".toByteArray()
        val newName = "updatedName"
        val newContentType = "application/pdf"
        every { file.bytes } returns fileBytes

        val expectedDto = DocumentMetadataDTO(
            id = metadataId,
            name = newName,
            size = fileBytes.size,
            contentType = newContentType,
            creationTimestamp = ZonedDateTime.now()
        )
        every { documentService.modify(metadataId, newName, newContentType, fileBytes) } returns expectedDto

        val result = controller.modify(metadataId, file, newName, newContentType)

        assertEquals(expectedDto, result)
        verify(exactly = 1) { documentService.modify(metadataId, newName, newContentType, fileBytes) }
    }

    /** --------------------delete----------------------- **/
    @Test
    fun `delete throws ResponseStatusException when metadataId is negative`() {
        val metadataId = -1

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.delete(metadataId)
        }
        assertEquals("id must be >= 0", exception.reason)
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `delete calls documentService delete when metadataId is valid`() {
        val metadataId = 1
        every { documentService.delete(metadataId) } returns Unit

        controller.delete(metadataId)

        verify(exactly = 1) { documentService.delete(metadataId) }
    }
}