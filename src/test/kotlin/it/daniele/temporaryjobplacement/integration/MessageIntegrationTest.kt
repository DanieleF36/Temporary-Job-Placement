package it.daniele.temporaryjobplacement.integration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.daniele.temporaryjobplacement.dtos.message.MessageDTO
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.State
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerIntegrationTest : IntegrationTest() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    val mapper = jacksonObjectMapper().apply { findAndRegisterModules() }

    /****************getAll*******************/
    @Test
    fun testGetAllDefaultFilter() {
        val response = restTemplate.getForEntity("/API/messages", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/messages should return 200 OK")
        val pageResponse: PageResponseDTO<MessageDTO> = mapper.readValue(response.body!!)
        assertEquals(2, pageResponse.totalElements, "Total elements should be 2")
        assertEquals(2, pageResponse.content.size, "Content size should be 2")

        assertEquals(10, pageResponse.size, "Page size should be 10")
        assertEquals(0, pageResponse.number, "Page number should be 0")

        val msg1 = pageResponse.content.find { it.id == 1 }
        assertNotNull(msg1, "Message with ID 1 should be present")
        assertEquals("Hello", msg1.subject, "Message 1 subject should be 'Hello'")
        assertEquals("First message body", msg1.body, "Message 1 body should be 'First message body'")
        assertEquals(State.RECEIVED, msg1.state, "Message 1 state should be RECEIVED")
        assertEquals(ZonedDateTime.parse("2025-04-13T14:00:00Z"), msg1.date, "Message 1 date should match")

        assertEquals(1, msg1.priority, "Message 1 priority should be 1")
        assertEquals(Channel.TEXT_MESSAGE, msg1.channel, "Message 1 channel should be TEXT_MESSAGE")
        assertEquals(1, msg1.senderId, "Sender ID for message 1 should be 1")

        val msg2 = pageResponse.content.find { it.id == 2 }
        assertNotNull(msg2, "Message with ID 2 should be present")
        assertEquals("Hi", msg2.subject, "Message 2 subject should be 'Hi'")
        assertEquals("Second message body", msg2.body, "Message 2 body should be 'Second message body'")
        assertEquals(State.READ, msg2.state, "Message 2 state should be READ")
        assertEquals(ZonedDateTime.parse("2025-04-13T15:00:00Z"), msg2.date, "Message 2 date should match")
        assertEquals(2, msg2.priority, "Message 2 priority should be 2")
        assertEquals(Channel.EMAIL, msg2.channel, "Message 2 channel should be EMAIL")
        assertEquals(2, msg2.senderId, "Sender ID for message 2 should be 2")
    }

    @Test
    fun testGetAllWithReadFilter() {
        val response = restTemplate.getForEntity("/API/messages?filter=read", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/messages?filter=read should return 200 OK")
        val pageResponse: PageResponseDTO<MessageDTO> = mapper.readValue(response.body!!)

        assertEquals(1, pageResponse.totalElements, "Total elements should be 1 with filter READ")
        assertEquals(1, pageResponse.content.size, "Content size should be 1")
        val msg = pageResponse.content[0]
        assertEquals(2, msg.id, "Message ID should be 2")
        assertEquals("Hi", msg.subject, "Subject should be 'Hi'")
        assertEquals("Second message body", msg.body, "Body should be 'Second message body'")
        assertEquals(State.READ, msg.state, "State should be READ")
        assertEquals(ZonedDateTime.parse("2025-04-13T15:00:00Z"), msg.date, "Date should match")
        assertEquals(2, msg.priority, "Priority should be 2")
        assertEquals(Channel.EMAIL, msg.channel, "Channel should be EMAIL")
        assertEquals(2, msg.senderId, "Sender ID for message 2 should be 2")
    }

    @Test
    fun testGetAllInvalidFilter() {
        val response = restTemplate.getForEntity("/API/messages?filter=invalid", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "Invalid filter should return 400 BAD_REQUEST")
    }

    @Test
    fun testGetAllInvalidPage() {
        val response = restTemplate.getForEntity("/API/messages?page=-1", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "Negative page number should return 400 BAD_REQUEST")
    }

    @Test
    fun testGetAllInvalidLimit() {
        val response = restTemplate.getForEntity("/API/messages?limit=0", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "Invalid limit should return 400 BAD_REQUEST")
    }

    @Test
    fun testGetAllInvalidSort(){
        val response = restTemplate.getForEntity("/API/messages?sort=invalid,asc", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "Invalid filter should return 400 BAD_REQUEST")
    }

    @Test
    fun testGetAllInvalidSortOrdering(){
        val response = restTemplate.getForEntity("/API/messages?sort=date,invalid", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "Invalid filter should return 400 BAD_REQUEST")
    }

}
