package it.daniele.temporaryjobplacement.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.entities.contact.Category
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContactIntegration: IntegrationTest() {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = jacksonObjectMapper().apply { findAndRegisterModules() }

    /** ------------------- getAll ------------------------ **/
    @Test
    fun `getAll throws BAD_REQUEST when page is negative`() {
        val response = restTemplate.getForEntity("/API/contacts?page=-1", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Page number must be >= 0", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when limit is zero`() {
        val response = restTemplate.getForEntity("/API/contacts?limit=0", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Limit number must be > 0", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when name is blank`() {
        val response = restTemplate.getForEntity("/API/contacts?name=   ", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when surname is blank`() {
        val response = restTemplate.getForEntity("/API/contacts?surname=	", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when email is blank`() {
        val response = restTemplate.getForEntity("/API/contacts?email= ", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when telephone is blank`() {
        val response = restTemplate.getForEntity("/API/contacts?telephone=", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when sort field not allowed`() {
        val response = restTemplate.getForEntity("/API/contacts?sort=foo,asc", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("Sort option not allowed: foo", json["message"].asText())
    }

    @Test
    fun `getAll throws BAD_REQUEST when sort direction invalid`() {
        val response = restTemplate.getForEntity("/API/contacts?sort=name,wrong", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        println(json)
        assertEquals("Ordering option not allowed: wrong", json["message"].asText())
    }

    @Test
    fun testGetAllWithNameFilter() {
        val response = restTemplate.getForEntity("/API/contacts?name=Luigi", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/contacts?name=Luigi should return 200 OK")
        val pageResponse: PageResponseDTO<ContactDTO> = mapper.readValue(response.body!!)
        assertEquals(1, pageResponse.totalElements, "Filtering by name Luigi should return 1 element")
        assertEquals("Luigi", pageResponse.content[0].name)
    }

    @Test
    fun testGetAllDefault() {
        val response = restTemplate.getForEntity("/API/contacts", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "GET /API/contacts should return 200 OK")
        val pageResponse: PageResponseDTO<ContactDTO> = mapper.readValue(response.body!!)
        assertEquals(2, pageResponse.totalElements, "Total elements should be 2")
        assertEquals(2, pageResponse.content.size, "Content size should be 2")
        assertEquals(10, pageResponse.size, "Page size should be 10")
        assertEquals(0, pageResponse.number, "Page number should be 0")

        val c1 = pageResponse.content.find { it.id == 1 }
        assertNotNull(c1, "Contact with ID 1 should be present")
        assertEquals("Mario", c1.name)
        assertEquals("Rossi", c1.surname)
        assertEquals(listOf("example1@example.com"), c1.email)
        assertEquals(listOf("Via Roma 1, Rome"), c1.address)
        assertEquals(listOf("39123456789"), c1.telephone)
        assertEquals("RSSMRA80A01H501U", c1.ssn)
        assertEquals(Category.CUSTOMER, c1.category)

        val c2 = pageResponse.content.find { it.id == 2 }
        assertNotNull(c2, "Contact with ID 2 should be present")
        assertEquals("Luigi", c2.name)
        assertEquals("Verdi", c2.surname)
        assertEquals(listOf("example2@example.com"), c2.email)
        assertEquals(listOf("Via Milano 2, Milan"), c2.address)
        assertEquals(listOf("39987654321"), c2.telephone)
        assertEquals("VRDLGU80B02H502U", c2.ssn)
        assertEquals(Category.PROFESSIONAL, c2.category)
    }

    /** ------------------- get ------------------------ **/
    @Test
    fun `get throws BAD_REQUEST when id is negative`() {
        val response = restTemplate.getForEntity("/API/contacts/-1", String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `get throws NOT_FOUND when id does not exist`() {
        val response = restTemplate.getForEntity("/API/contacts/9999", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("id not found", json["message"].asText())
    }

    @Test
    fun `get returns OK when id exists`() {
        val response = restTemplate.getForEntity("/API/contacts/1", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val contact: ContactDTO = mapper.readValue(response.body!!)
        assertEquals(1, contact.id)
        assertEquals("Mario", contact.name)
        assertEquals("Rossi", contact.surname)
        assertEquals(listOf("example1@example.com"), contact.email)
        assertEquals(listOf("Via Roma 1, Rome"), contact.address)
        assertEquals(listOf("39123456789"), contact.telephone)
        assertEquals("RSSMRA80A01H501U", contact.ssn)
        assertEquals(Category.CUSTOMER, contact.category)
    }

    /** ------------------- create ------------------------ **/
    private fun post(dto: ContactDTO): ResponseEntity<String> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(dto, headers)
        return restTemplate.postForEntity("/API/contacts", request, String::class.java)
    }

    @Test
    fun `create throws BAD_REQUEST when name is blank`() {
        val dto = ContactDTO(
            name = "",
            surname = "Test",
            email = listOf("test@example.com"),
            address = listOf("Via Test 1"),
            telephone = listOf("391234567890"),
            ssn = null,
            category = Category.UNKNOWN
        )
        val response = post(dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        println(json)
        assertEquals("name must not be blank", json["message"].asText())
    }

    @Test
    fun `create throws BAD_REQUEST when surname is blank`() {
        val dto = ContactDTO(
            name = "Test",
            surname = "",
            email = listOf("test@example.com"),
            address = listOf("Via Test 1"),
            telephone = listOf("391234567890"),
            ssn = null,
            category = Category.UNKNOWN
        )
        val response = post(dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("surname must not be blank", json["message"].asText())
    }

    @Test
    fun `create throws BAD_REQUEST when email contains blank`() {
        val dto = ContactDTO(
            name = "Test",
            surname = "User",
            email = listOf(""),
            address = listOf("Via Test 1"),
            telephone = listOf("391234567890"),
            ssn = null,
            category = Category.UNKNOWN
        )
        val response = post(dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("email List elements must not be blank", json["message"].asText())
    }

    @Test
    fun `create throws BAD_REQUEST when address contains blank`() {
        val dto = ContactDTO(
            name = "Test",
            surname = "User",
            email = listOf("test@example.com"),
            address = listOf(""),
            telephone = listOf("391234567890"),
            ssn = null,
            category = Category.UNKNOWN
        )
        val response = post(dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("address List elements must not be blank", json["message"].asText())
    }

    @Test
    fun `create throws BAD_REQUEST when telephone contains blank`() {
        val dto = ContactDTO(
            name = "Test",
            surname = "User",
            email = listOf("test@example.com"),
            address = listOf("Via Test 1"),
            telephone = listOf(""),
            ssn = null,
            category = Category.UNKNOWN
        )
        val response = post(dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("telephone List elements must not be blank", json["message"].asText())
    }

    @Test
    fun `create returns CREATED and get retrieves contact`() {
        val dto = ContactDTO(
            name = "Anna",
            surname = "Bianchi",
            email = listOf("anna.bianchi@example.com"),
            address = listOf("Via Firenze 3"),
            telephone = listOf("393212345678"),
            ssn = "BNCHNA80C22H501X",
            category = Category.CUSTOMER
        )
        val createResponse = post(dto)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val created: ContactDTO = mapper.readValue(createResponse.body!!)
        assertTrue(created.id > 0)

        val getResponse = restTemplate.getForEntity("/API/contacts/${created.id}", String::class.java)
        assertEquals(HttpStatus.OK, getResponse.statusCode)
        val fetched: ContactDTO = mapper.readValue(getResponse.body!!)
        assertEquals(created, fetched)
    }
}