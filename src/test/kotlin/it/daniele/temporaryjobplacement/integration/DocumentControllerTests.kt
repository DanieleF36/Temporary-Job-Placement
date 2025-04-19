package it.daniele.temporaryjobplacement.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.daniele.temporaryjobplacement.dtos.document.DocumentMetadataDTO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.http.HttpEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentIntegration : IntegrationTest() {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = jacksonObjectMapper().apply { findAndRegisterModules() }

    @Test
    fun `getAll throws BAD_REQUEST when page is negative`() {
        val response = restTemplate.getForEntity("/API/documents?page=-1&sort=name,asc", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Page number must be >= 0", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when limit is zero`() {
        val response = restTemplate.getForEntity("/API/documents?limit=0&sort=name,asc", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Limit number must be > 0", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when sort field not allowed`() {
        val response = restTemplate.getForEntity("/API/documents?sort=foo,asc", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Sort option not allowed: foo", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when sort direction invalid`() {
        val response = restTemplate.getForEntity("/API/documents?sort=name,wrong", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Ordering option not allowed: wrong", json["message"].asText())
    }

    @Test
    fun `getAll returns page`() {
        val response = restTemplate.getForEntity("/API/documents?sort=name,asc", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val pageResponse: PageResponseDTO<DocumentMetadataDTO> = mapper.readValue(response.body!!)
        assertEquals(2, pageResponse.totalElements)
        assertEquals(2, pageResponse.content.size)

        val d1 = pageResponse.content.find { it.id == 1 }!!
        assertEquals("Document 1", d1.name)
        assertEquals(4, d1.size)
        assertEquals("application/octet-stream", d1.contentType)

        val d2 = pageResponse.content.find { it.id == 2 }!!
        assertEquals("Document 2", d2.name)
        assertEquals(4, d2.size)
        assertEquals("application/pdf", d2.contentType)
    }

    @Test
    fun `get throws BAD_REQUEST when id is negative`() {
        val response = restTemplate.getForEntity("/API/documents/-1", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Id must be >= 0", json["message"].asText())
    }

    @Test
    fun `get throws NOT_FOUND when id does not exist`() {
        val response = restTemplate.getForEntity("/API/documents/9999", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("id not found", json["message"].asText())
    }

    @Test
    fun `get returns OK when id exists`() {
        val response = restTemplate.getForEntity("/API/documents/1", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val doc: DocumentMetadataDTO = mapper.readValue(response.body!!)
        assertEquals(1, doc.id)
        assertEquals("Document 1", doc.name)
        assertEquals(4, doc.size)
        assertEquals("application/octet-stream", doc.contentType)
        assertNotNull(doc.creationTimestamp)
    }

    @Test
    fun `getData throws BAD_REQUEST when id is negative`() {
        val response = restTemplate.getForEntity("/API/documents/-1/data", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Id must be >= 0", json["message"].asText())
    }

    @Test
    fun `getData throws NOT_FOUND when id does not exist`() {
        val response = restTemplate.getForEntity("/API/documents/9999/data", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("id not found", json["message"].asText())
    }

    @Test
    fun `getData returns binary data with correct headers`() {
        val response = restTemplate.getForEntity("/API/documents/1/data", ByteArray::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("application/octet-stream", response.headers.contentType.toString())
        assertEquals("attachment; filename=\"Document 1\"", response.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION))
        val expected = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        assertTrue(response.body!!.contentEquals(expected))
    }

    @Test
    fun `create returns CREATED and document can be retrieved`() {
        val resource = object : ByteArrayResource("hello".toByteArray()) {
            override fun getFilename() = "new.txt"
        }
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { add("file", resource) }
        val request = HttpEntity(body, headers)

        val createResponse = restTemplate.postForEntity("/API/documents", request, String::class.java)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val created: DocumentMetadataDTO = mapper.readValue(createResponse.body!!)
        assertTrue(created.id!! > 2)
        assertEquals("new.txt", created.name)
        assertEquals(5, created.size)

        val getResponse = restTemplate.getForEntity("/API/documents/${created.id}", String::class.java)
        assertEquals(HttpStatus.OK, getResponse.statusCode)
    }

    @Test
    fun `modify throws BAD_REQUEST when id is negative`() {
        val dto = mapOf("name" to "X", "contentType" to "Y", "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/-1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Id must be >= 0", json["message"].asText())
    }

    @Test
    fun `modify throws BAD_REQUEST when name is blank`() {
        val dto = mapOf("name" to "   ", "contentType" to null, "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("name If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `modify throws BAD_REQUEST when contentType is blank`() {
        val dto = mapOf("name" to null, "contentType" to "   ", "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contentType If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `modify returns OK when updating name only`() {
        val dto = mapOf("name" to "Renamed", "contentType" to null, "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val updated: DocumentMetadataDTO = mapper.readValue(response.body!!)
        assertEquals(1, updated.id)
        assertEquals("Renamed", updated.name)
        assertEquals(4, updated.size)
        assertEquals("application/octet-stream", updated.contentType)
    }

    @Test
    fun `modify returns OK when updating contentType only`() {
        val dto = mapOf("name" to null, "contentType" to "application/test", "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val updated: DocumentMetadataDTO = mapper.readValue(response.body!!)
        assertEquals(1, updated.id)
        assertEquals("Document 1", updated.name)
        assertEquals(4, updated.size)
        assertEquals("application/test", updated.contentType)
    }

    @Test
    fun `modify returns OK when updating name and contentType`() {
        val dto = mapOf("name" to "UpdatedName", "contentType" to "application/data", "file" to null)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapper.writeValueAsString(dto), headers)

        val response = restTemplate.exchange("/API/documents/1", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val updated: DocumentMetadataDTO = mapper.readValue(response.body!!)
        assertEquals(1, updated.id)
        assertEquals("UpdatedName", updated.name)
        assertEquals(4, updated.size)
        assertEquals("application/data", updated.contentType)
    }

    @Test
    fun `delete throws BAD_REQUEST when id is negative`() {
        val response = restTemplate.exchange(
            "/API/documents/-1",
            HttpMethod.DELETE,
            HttpEntity(null, HttpHeaders()),
            ByteArray::class.java
        )
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Id must be >= 0", json["message"].asText())
    }

    @Test
    fun `delete removes document successfully`() {
        val delResponse = restTemplate.exchange(
            "/API/documents/1",
            HttpMethod.DELETE,
            HttpEntity(null, HttpHeaders()),
            ByteArray::class.java
        )

        assertEquals(HttpStatus.OK, delResponse.statusCode)

        val getResponse = restTemplate.getForEntity("/API/documents/1", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }
}
