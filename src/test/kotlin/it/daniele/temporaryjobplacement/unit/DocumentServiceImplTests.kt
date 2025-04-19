package it.daniele.temporaryjobplacement.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.daniele.temporaryjobplacement.entities.DocumentBinaryData
import it.daniele.temporaryjobplacement.entities.DocumentMetadata
import it.daniele.temporaryjobplacement.exceptions.DocumentNameAlreadyExists
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.repositories.DocumentBinaryRepository
import it.daniele.temporaryjobplacement.repositories.DocumentMetadataRepository
import it.daniele.temporaryjobplacement.services.DocumentServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.ZonedDateTime
import java.util.Optional

internal class DocumentServiceImplTests {
    private val repoMeta: DocumentMetadataRepository = mockk()
    private val repoBinary: DocumentBinaryRepository = mockk()
    private val service = DocumentServiceImpl(repoMeta, repoBinary)

    /** --------------------getAll----------------------- **/
    @Test
    fun `getAll throws IllegalArgumentException when page is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.getAll(-1, 10, Sort.by(Sort.Direction.ASC, "name"))
        }
    }

    @Test
    fun `getAll throws IllegalArgumentException when limit is less or equal to zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.getAll(0, 0, Sort.by(Sort.Direction.ASC, "name"))
        }
    }

    @Test
    fun `getAll returns page of DocumentMetadataDTO`() {
        val page = 0
        val limit = 10
        val sort = Sort.by(Sort.Direction.ASC, "name")
        val meta = DocumentMetadata(
            name = "doc1",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        meta.binaryContent = null
        val metaList = listOf(meta)
        val pageable = PageRequest.of(page, limit, sort)
        val pageImpl: Page<DocumentMetadata> = PageImpl(metaList, pageable, 1)
        every { repoMeta.findAll(pageable) } returns pageImpl

        val result = service.getAll(page, limit, sort)

        assertEquals(1, result.totalElements)
        assertEquals("doc1", result.content[0].name)
    }

    /** --------------------get----------------------- **/
    @Test
    fun `get throws IllegalArgumentException when id is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.get(-1)
        }
    }

    @Test
    fun `get returns null when document not found`() {
        val id = 1
        every { repoMeta.findById(id) } returns Optional.empty()

        val result = service.get(id)

        assertNull(result)
    }

    @Test
    fun `get returns DocumentMetadataDTO when document is found`() {
        val id = 1
        val meta = DocumentMetadata(
            name = "doc1",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        meta.binaryContent = null
        every { repoMeta.findById(id) } returns Optional.of(meta)

        val result = service.get(id)

        assertNotNull(result)
        assertEquals("doc1", result!!.name)
    }

    /** --------------------getData----------------------- **/
    @Test
    fun `getData returns null when binary data not found`() {
        val id = 1
        every { repoBinary.findById(id) } returns Optional.empty()

        val result = service.getData(id)

        assertNull(result)
    }

    @Test
    fun `getData returns DocumentBinaryDataDTO when binary data is found`() {
        val id = 1
        val meta = DocumentMetadata(
            name = "doc1",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        val binaryData = DocumentBinaryData(
            content = "binary".toByteArray(),
            metadata = meta
        )
        meta.binaryContent = binaryData
        every { repoBinary.findById(id) } returns Optional.of(binaryData)

        val result = service.getData(id)

        assertNotNull(result)
        assertEquals("doc1", result!!.metadataDTO.name)
        assertArrayEquals("binary".toByteArray(), result.content)
    }

    /** --------------------create----------------------- **/
    @Test
    fun `create throws IllegalArgumentException when name is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.create("", "application/pdf", "content".toByteArray())
        }
    }

    @Test
    fun `create throws IllegalArgumentException when contentType is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.create("doc", "", "content".toByteArray())
        }
    }

    @Test
    fun `create throws DocumentNameAlreadyExists when duplicate name exists`() {
        val name = "doc"
        every { repoMeta.findByName(name) } returns DocumentMetadata(
            name = name,
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )

        val exception = assertThrows(DocumentNameAlreadyExists::class.java) {
            service.create(name, "application/pdf", "content".toByteArray())
        }
        assertEquals("Duplicate name already exists", exception.message)
    }

    @Test
    fun `create returns DocumentMetadataDTO when valid parameters provided`() {
        val name = "doc"
        val contentType = "application/pdf"
        val binaryContent = "content".toByteArray()
        every { repoMeta.findByName(name) } returns null
        every { repoMeta.save(any()) } answers { firstArg() }

        val result = service.create(name, contentType, binaryContent)

        assertNotNull(result)
        assertEquals(0, result.id)
        assertEquals(name, result.name)
        assertEquals(binaryContent.size, result.size)
        assertEquals(contentType, result.contentType)
    }

    /** --------------------modify----------------------- **/
    @Test
    fun `modify throws IllegalArgumentException when metadataId is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.modify(-1, "newName", "application/pdf", "newContent".toByteArray())
        }
    }

    @Test
    fun `modify throws IllegalArgumentException when name is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.modify(1, "   ", "application/pdf", "newContent".toByteArray())
        }
    }

    @Test
    fun `modify throws IllegalArgumentException when contentType is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.modify(1, "newName", "   ", "newContent".toByteArray())
        }
    }

    @Test
    fun `modify throws NotFoundException when document not found`() {
        val id = 1
        every { repoMeta.findById(id) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.modify(id, "newName", "application/pdf", "newContent".toByteArray())
        }
        assertEquals("File not found: $id", exception.message)
    }

    @Test
    fun `modify returns DocumentMetadataDTO when valid parameters provided`() {
        val id = 1
        val originalMeta = DocumentMetadata(
            name = "oldName",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        val originalContent = "original".toByteArray()
        val originalBinary = DocumentBinaryData(
            content = originalContent,
            metadata = originalMeta
        )
        originalMeta.binaryContent = originalBinary
        every { repoMeta.findById(id) } returns Optional.of(originalMeta)
        every { repoMeta.save(any()) } answers { firstArg() }

        val result = service.modify(id, "newName", "application/pdf", "newContent".toByteArray())

        assertNotNull(result)
        assertEquals("newName", result.name)
        assertEquals("application/pdf", result.contentType)
        assertEquals("newContent".toByteArray().size, result.size)
    }

    /** --------------------delete----------------------- **/
    @Test
    fun `delete throws IllegalArgumentException when id is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.delete(-1)
        }
    }

    @Test
    fun `delete throws NotFoundException when document not found`() {
        val id = 1
        every { repoMeta.findById(id) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.delete(id)
        }
        assertEquals("File not found: $id", exception.message)
    }

    @Test
    fun `delete calls deleteById when document exists`() {
        val id = 1
        val meta = DocumentMetadata(
            name = "doc",
            size = 100,
            contentType = "application/pdf",
            creationTimestamp = ZonedDateTime.now()
        )
        meta.binaryContent = null
        every { repoMeta.findById(id) } returns Optional.of(meta)
        every { repoMeta.deleteById(id) } returns Unit

        service.delete(id)

        verify(exactly = 1) { repoMeta.deleteById(id) }
    }
}
