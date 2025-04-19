package it.daniele.temporaryjobplacement.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.daniele.temporaryjobplacement.dtos.message.ActionDTO
import it.daniele.temporaryjobplacement.dtos.MessageDTO
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
class MessageIntegrationTest : IntegrationTest() {

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

    /****************get*******************/
    @Test
    fun testGetExistingMessage() {
        val response = restTemplate.getForEntity("/API/messages/1", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/messages/1 should return 200 OK")
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertEquals(1, msg.id, "Message ID should be 1")
        assertEquals("Hello", msg.subject, "Subject should be 'Hello'")
        assertEquals("First message body", msg.body, "Body should be 'First message body'")
        assertEquals(State.RECEIVED, msg.state, "State should be RECEIVED")
        assertEquals(ZonedDateTime.parse("2025-04-13T14:00:00Z"), msg.date, "Date should match")
        assertEquals(1, msg.priority, "Priority should be 1")
        assertEquals(Channel.TEXT_MESSAGE, msg.channel, "Channel should be TEXT_MESSAGE")
        assertEquals(1, msg.senderId, "Sender ID should be 1")

    }

    @Test
    fun testGetNonExistingMessage() {
        val response = restTemplate.getForEntity("/API/messages/999", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "GET non-existing message should return 404 NOT_FOUND")
    }

    @Test
    fun testGetInvalidId() {
        val response = restTemplate.getForEntity("/API/messages/0", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "GET with invalid ID should return 400 BAD_REQUEST")
    }

    /****************create*******************/
    @Test
    fun testCreateValidMessage() {
        val json = """{"senderId": 1, "channel": "TEXT_MESSAGE", "subject": "Test subject", "body" :"Test body", "date":"2025-04-13T16:00:00Z"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertTrue(msg.id > 0, "Created message should have an ID greater than 0")
        assertEquals("Test subject", msg.subject, "Subject should match input")
        assertEquals("Test body", msg.body, "Body should match input")
        assertEquals(State.RECEIVED, msg.state, "State should default to RECEIVED")
        assertEquals(ZonedDateTime.parse("2025-04-13T16:00:00Z"), msg.date, "Date should match input")
        assertEquals(0, msg.priority, "Default priority should be 0")
        assertEquals(Channel.TEXT_MESSAGE, msg.channel, "Channel should match input")
        assertEquals(1, msg.senderId, "Sender ID should be 1")
    }

    @Test
    fun testCreateInvalidSenderId() {
        val json = """[0, "TEXT_MESSAGE", "Test subject", "Test body", "2025-04-13T16:00:00Z"]"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "POST with invalid sender ID should return 400 BAD_REQUEST")
    }

    @Test
    fun testCreateInvalidChannel(){
        val json = """[1, "MESSAGE", "Test subject", "Test body", "2025-04-13T16:00:00Z"]"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "POST with invalid sender ID should return 400 BAD_REQUEST")
    }

    @Test
    fun testCreateNullSubject(){
        val json = """{"senderId": 1, "channel": "TEXT_MESSAGE", "subject": null, "body": "Test body", "date": "2025-04-13T16:00:00Z"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode, "POST with valid data should return 200 OK ${response.body}")
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertTrue(msg.id > 0, "Created message should have an ID greater than 0")
        assertEquals(null, msg.subject, "Subject should match input")
        assertEquals("Test body", msg.body, "Body should match input")
        assertEquals(State.RECEIVED, msg.state, "State should default to RECEIVED")
        assertEquals(ZonedDateTime.parse("2025-04-13T16:00:00Z"), msg.date, "Date should match input")
        assertEquals(0, msg.priority, "Default priority should be 0")
        assertEquals(Channel.TEXT_MESSAGE, msg.channel, "Channel should match input")
        assertEquals(1, msg.senderId, "Sender ID should be 1")
    }

    @Test
    fun testCreateNullBody(){
        val json = """{"senderId": 1, "channel": "TEXT_MESSAGE", "subject": "Test subject", "body": null, "date": "2025-04-13T16:00:00Z"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode, "POST with valid data should return 200 OK ${response.body}")
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertTrue(msg.id > 0, "Created message should have an ID greater than 0")
        assertEquals("Test subject", msg.subject, "Subject should match input")
        assertEquals(null, msg.body, "Body should match input")
        assertEquals(State.RECEIVED, msg.state, "State should default to RECEIVED")
        assertEquals(ZonedDateTime.parse("2025-04-13T16:00:00Z"), msg.date, "Date should match input")
        assertEquals(0, msg.priority, "Default priority should be 0")
        assertEquals(Channel.TEXT_MESSAGE, msg.channel, "Channel should match input")
        assertEquals(1, msg.senderId, "Sender ID should be 1")
    }

    @Test
    fun testCreateSenderNotFound() {
        val json = """{"senderId": 999, "channel": "TEXT_MESSAGE", "subject": "Test subject", "body": null, "date": "2025-04-13T16:00:00Z"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages", request, String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "POST with non-existing sender should return 404 NOT_FOUND")
    }

    /****************changeState*******************/
    @Test
    fun testChangeStateValid() {
        val json = """{"newState": "READ", "comment": "comment"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages/1", request, String::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode, "POST to change state should return 200 OK")
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertEquals(1, msg.id, "Message ID should remain 1")
        assertEquals("READ", msg.state.toString(), "State should be updated to READ")
    }

    @Test
    fun testChangeStateInvalidId() {
        val json = """{"newState": "invalid", "comment": "comment"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages/0", request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "POST with invalid ID should return 400 BAD_REQUEST")
    }

    @Test
    fun testChangeStateMessageNotFound() {
        val json = """{"newState": "READ", "comment": "comment"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages/999", request, String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "POST for non-existing message should return 404 NOT_FOUND")
    }

    @Test
    fun testChangeStateInvalidTransition() {
        val json = """{"newState": "DONE", "comment": "comment"}"""
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(json, headers)
        val response = restTemplate.postForEntity("/API/messages/1", request, String::class.java)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode, "Invalid state transition should return 422 UNPROCESSABLE_ENTITY")
    }

    /****************getHistory*******************/
    @Test
    fun testGetHistory() {
        val response = restTemplate.getForEntity("/API/messages/1/history", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/messages/2/history should return 200 OK")
        val history: List<ActionDTO> = mapper.readValue(response.body!!)
        assertEquals(1, history.size, "History should have just 1 action")
        assertEquals("Action comment 1", history[0].comment, "History comment is different")
    }

    @Test
    fun testGetHistoryInvalidId() {
        val response = restTemplate.getForEntity("/API/messages/0/history", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "GET /API/messages/0/history should return 400 BAD_REQUEST")
    }

    @Test
    fun testGetHistoryMessageNotFound() {
        val response = restTemplate.getForEntity("/API/messages/999/history", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "GET history for non-existing message should return 404 NOT_FOUND")
    }

    /****************changePriority*******************/
    @Test
    fun testChangePriorityValid() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity("5", headers)
        val response = restTemplate.exchange("/API/messages/1/priority", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "PUT with valid data should return 200 OK")
        val msg: MessageDTO = mapper.readValue(response.body!!)
        assertEquals(5, msg.priority, "Priority should be updated to 5")
        assertEquals(1, msg.id, "Message ID should remain 1")

        assertEquals("Hello", msg.subject, "Subject should remain unchanged")
        assertEquals("First message body", msg.body, "Body should remain unchanged")
        assertEquals(State.RECEIVED, msg.state, "State should remain unchanged")
        assertEquals(Channel.TEXT_MESSAGE, msg.channel, "Channel should remain unchanged")
    }

    @Test
    fun testChangePriorityInvalidId() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity("-5", headers)
        val response = restTemplate.exchange("/API/messages/0/priority", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "PUT with invalid ID should return 400 BAD_REQUEST")
    }

    @Test
    fun testChangePriorityInvalidPriority() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity("-1", headers)
        val response = restTemplate.exchange("/API/messages/1/priority", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, "PUT with negative priority should return 400 BAD_REQUEST")
    }

    @Test
    fun testChangePriorityMessageNotFound() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity("5", headers)
        val response = restTemplate.exchange("/API/messages/999/priority", HttpMethod.PUT, request, String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode, "PUT for non-existing message should return 404 NOT_FOUND")
    }
}
