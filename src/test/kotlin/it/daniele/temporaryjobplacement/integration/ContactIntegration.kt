package it.daniele.temporaryjobplacement.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.daniele.temporaryjobplacement.dtos.contact.ContactDTO
import it.daniele.temporaryjobplacement.dtos.UpdateContactDTO
import it.daniele.temporaryjobplacement.dtos.contact.AddressDTO
import it.daniele.temporaryjobplacement.dtos.contact.EmailDTO
import it.daniele.temporaryjobplacement.dtos.contact.TelephoneDTO
import it.daniele.temporaryjobplacement.entities.contact.Category
import org.junit.jupiter.api.Assertions.assertFalse
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
        val response = restTemplate.getForEntity("/API/contacts?surname=\t", String::class.java)
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
        assertEquals("Mario", c1!!.name)
        assertEquals("Rossi", c1.surname)
        assertEquals(listOf("example1@example.com"), c1.email.map { it.email })
        assertEquals(listOf("Via Roma 1, Rome"), c1.address.map { it.address })
        assertEquals(listOf("39123456789"), c1.telephone.map { it.prefix + it.number })
        assertEquals("RSSMRA80A01H501U", c1.ssn)
        assertEquals(Category.CUSTOMER, c1.category)

        val c2 = pageResponse.content.find { it.id == 2 }
        assertNotNull(c2, "Contact with ID 2 should be present")
        assertEquals("Luigi", c2!!.name)
        assertEquals("Verdi", c2.surname)
        assertEquals(listOf("example2@example.com"), c2.email.map { it.email })
        assertEquals(listOf("Via Milano 2, Milan"), c2.address.map { it.address })
        assertEquals(listOf("39987654321"), c2.telephone.map { it.prefix + it.number })
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
        assertEquals(listOf("example1@example.com"), contact.email.map { it.email })
        assertEquals(listOf("Via Roma 1, Rome"), contact.address.map { it.address })
        assertEquals(listOf("39123456789"), contact.telephone.map { it.prefix + it.number })
        assertEquals("RSSMRA80A01H501U", contact.ssn)
        assertEquals(Category.CUSTOMER, contact.category)
    }

    /** ------------------- update ------------------------ **/
    private fun update(id: Int, dto: UpdateContactDTO): ResponseEntity<String> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(dto, headers)
        return restTemplate.exchange("/API/contacts/$id", HttpMethod.PUT, request, String::class.java)
    }

    @Test
    fun `update throws BAD_REQUEST when id is negative`() {
        val dto = UpdateContactDTO(name = "New", surname = "Name", ssn = null)
        val response = update(-1, dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `update throws BAD_REQUEST when name is blank`() {
        val dto = UpdateContactDTO(name = "", surname = "Name", ssn = null)
        val response = update(1, dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("name If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `update throws BAD_REQUEST when surname is blank`() {
        val dto = UpdateContactDTO(name = "New", surname = "", ssn = null)
        val response = update(1, dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("surname If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `update throws BAD_REQUEST when ssn is blank`() {
        val dto = UpdateContactDTO(name = "New", surname = "Name", ssn = "")
        val response = update(1, dto)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("ssn If present, the field must be not blank", json["message"].asText())
    }

    @Test
    fun `update throws NOT_FOUND when id does not exist`() {
        val dto = UpdateContactDTO(name = "New", surname = "Name", ssn = null)
        val response = update(9999, dto)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `update returns OK when updating contact`() {
        val dto = UpdateContactDTO(name = "Luigi", surname = "Bianchi", ssn = "BIANLU80D03H501Y")
        val updateResponse = update(1, dto)
        assertEquals(HttpStatus.OK, updateResponse.statusCode)

        val getResponse = restTemplate.getForEntity("/API/contacts/1", String::class.java)
        assertEquals(HttpStatus.OK, getResponse.statusCode)
        val updated: ContactDTO = mapper.readValue(getResponse.body!!)
        assertEquals(1, updated.id)
        assertEquals("Luigi", updated.name)
        assertEquals("Bianchi", updated.surname)
        assertEquals("BIANLU80D03H501Y", updated.ssn)

        assertEquals(listOf("example1@example.com"), updated.email.map { it.email })
        assertEquals(listOf("Via Roma 1, Rome"), updated.address.map { it.address })
        assertEquals(listOf("39123456789"), updated.telephone.map { it.prefix + it.number })
        assertEquals(Category.CUSTOMER, updated.category)
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
                email = listOf(),
                address = listOf(),
                telephone = listOf(),
                ssn = null,
                category = Category.UNKNOWN
            )
            val response = post(dto)
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            val json = mapper.readTree(response.body!!)
            assertEquals("name must not be blank", json["message"].asText())
        }
    
        @Test
        fun `create throws BAD_REQUEST when surname is blank`() {
            val dto = ContactDTO(
                name = "Test",
                surname = "",
                email = listOf(),
                address = listOf(),
                telephone = listOf(),
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
                email = listOf(EmailDTO(0, "")),
                address = listOf(),
                telephone = listOf(),
                ssn = null,
                category = Category.UNKNOWN
            )
            val response = post(dto)
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            val json = mapper.readTree(response.body!!)
            assertEquals("email[0].email must not be blank", json["message"].asText())
        }
    
        @Test
        fun `create throws BAD_REQUEST when address contains blank`() {
            val dto = ContactDTO(
                name = "Test",
                surname = "User",
                email = listOf(EmailDTO(0, "test@example.com")),
                address = listOf(AddressDTO(0, "")),
                telephone = listOf(),
                ssn = null,
                category = Category.UNKNOWN
            )
            val response = post(dto)
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            val json = mapper.readTree(response.body!!)
            assertEquals("address[0].address must not be blank", json["message"].asText())
        }
    
        @Test
        fun `create throws BAD_REQUEST when telephone contains blank`() {
            val dto = ContactDTO(
                name = "Test",
                surname = "User",
                email = listOf(EmailDTO(0, "test@example.com")),
                address = listOf(AddressDTO(0, "Via Test 1")),
                telephone = listOf(TelephoneDTO(0, "39", "")),
                ssn = null,
                category = Category.UNKNOWN
            )
            val response = post(dto)
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            val json = mapper.readTree(response.body!!)
            assertEquals("telephone[0].number must not be blank", json["message"].asText())
        }
    
        @Test
        fun `create returns CREATED and get retrieves contact`() {
            val dto = ContactDTO(
                name = "Anna",
                surname = "Bianchi",
                email = listOf(EmailDTO(0, "anna.bianchi@example.com")),
                address = listOf(AddressDTO(0, "Via Firenze 3")),
                telephone = listOf(TelephoneDTO(0, "39", "3212345678")),
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

    /** ------------------- delete ------------------------ **/
    private fun delete(id: Int) = restTemplate.exchange(
        "/API/contacts/$id", HttpMethod.DELETE,
        HttpEntity(null, HttpHeaders()), String::class.java
    )

    @Test
    fun `delete throws BAD_REQUEST when id is negative`() {
        val response = delete(-1)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `delete throws NOT_FOUND when id does not exist`() {
        val response = delete(9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `delete removes contact and get returns NOT_FOUND`() {
        val deleteResponse = delete(1)
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        val getResponse = restTemplate.getForEntity("/API/contacts/1", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    /** ------------------- addEmail ------------------------ **/
    private fun addEmail(id: Int, email: String) = restTemplate.postForEntity(
        "/API/contacts/$id/emails",
        HttpEntity(email, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
        String::class.java
    )

    @Test
    fun `addEmail throws BAD_REQUEST when id is negative`() {
        val response = addEmail(-1, "new@example.com")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `addEmail throws BAD_REQUEST when email is blank`() {
        val response = addEmail(1, "")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `addEmail throws NOT_FOUND when id does not exist`() {
        val response = addEmail(9999, "new@example.com")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `addEmail create a new email and can be retrieved`() {
        val newEmail = "new@example.com"
        val createResponse = addEmail(1, newEmail)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)

        val updated = mapper.readValue<ContactDTO>(
            restTemplate.getForEntity("/API/contacts/1", String::class.java).body!!
        )
        assertTrue(updated.email.any { it.email == newEmail })
    }

    @Test
    fun `addEmail appends an existing email and can be retrieved`() {
        val newEmail = "example2@example.com"
        val createResponse = addEmail(1, newEmail)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)

        val updated = mapper.readValue<ContactDTO>(
            restTemplate.getForEntity("/API/contacts/1", String::class.java).body!!
        )
        assertTrue(updated.email.any { it.email == newEmail })
    }

    /** ------------------- changeEmail ------------------------ **/
    private fun changeEmail(contactId: Int, emailId: Int, newEmail: String) = restTemplate.exchange(
        "/API/contacts/$contactId/emails/$emailId",
        HttpMethod.PUT,
        HttpEntity(newEmail, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
        String::class.java
    )
    private fun getContact(contactId: Int): ContactDTO = mapper.readValue(
        restTemplate.getForEntity("/API/contacts/$contactId", String::class.java).body!!
    )

    @Test
    fun `changeEmail throws BAD_REQUEST when contactId is negative`() {
        val response = changeEmail(-1, 1, "new@example.com")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `changeEmail throws BAD_REQUEST when emailId is negative`() {
        val response = changeEmail(1, -1, "new@example.com")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("emailId must be >= 0", json["message"].asText())
    }

    @Test
    fun `changeEmail throws BAD_REQUEST when newEmail is blank`() {
        val response = changeEmail(1, 1, "")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `changeEmail throws NOT_FOUND when contact does not exist`() {
        val response = changeEmail(9999, 1, "new@example.com")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `changeEmail throws NOT_FOUND when email does not exist`() {
        val response = changeEmail(1, 9999, "new@example.com")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("email not found", json["message"].asText())
    }

    @Test
    fun `changeEmail updates existing email when unique`() {
        val newEmail = "unique_changed@example.com"
        val response = changeEmail(1, 1, newEmail)
        assertEquals(HttpStatus.OK, response.statusCode)

        val updated = getContact(1)
        assertTrue(updated.email.any { it.email == newEmail })
        assertFalse(updated.email.any { it.email == "example1@example.com" })
    }

    @Test
    fun `changeEmail duplicates and updates email when shared`() {
        restTemplate.postForEntity(
            "/API/contacts/2/emails",
            HttpEntity("example1@example.com", HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
            String::class.java
        )

        val newEmail = "shared_changed@example.com"
        val response = changeEmail(2, 1, newEmail)
        assertEquals(HttpStatus.OK, response.statusCode)

        val contact1 = getContact(1)
        assertTrue(contact1.email.any { it.email == "example1@example.com" })

        val contact2 = getContact(2)
        assertTrue(contact2.email.any { it.email == newEmail })
        assertFalse(contact2.email.any { it.email == "example1@example.com" })
    }

    /** ------------------- deleteEmail ------------------------ **/
    private fun deleteEmail(contactId: Int, emailId: Int) = restTemplate.exchange(
        "/API/contacts/$contactId/emails/$emailId",
        HttpMethod.DELETE,
        HttpEntity(null, HttpHeaders()),
        String::class.java
    )

    @Test
    fun `deleteEmail throws BAD_REQUEST when contactId is negative`() {
        val response = deleteEmail(-1, 1)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `deleteEmail throws BAD_REQUEST when emailId is negative`() {
        val response = deleteEmail(1, -1)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("emailId must be >= 0", json["message"].asText())
    }

    @Test
    fun `deleteEmail throws NOT_FOUND when contact does not exist`() {
        val response = deleteEmail(9999, 1)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `deleteEmail throws NOT_FOUND when email does not exist`() {
        val response = deleteEmail(1, 9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("email not found", json["message"].asText())
    }

    @Test
    fun `deleteEmail removes email and can be retrieved`() {
        restTemplate.postForEntity(
            "/API/contacts/1/emails",
            HttpEntity("temp@example.com", HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
            String::class.java
        )
        val before = getContact(1)
        val emailToDelete = before.email.first().id

        val delResp = deleteEmail(1, emailToDelete)
        assertEquals(HttpStatus.OK, delResp.statusCode)

        val after = getContact(1)
        assertFalse(after.email.any { it.id == emailToDelete })
    }

    /** ------------------- modifyCategory ------------------------ **/
    private fun modifyCategory(contactId: Int, category: Category) = restTemplate.exchange(
        "/API/contacts/$contactId/category",
        HttpMethod.PUT,
        HttpEntity(category, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
        String::class.java
    )

    @Test
    fun `modifyCategory throws BAD_REQUEST when contactId is negative`() {
        val response = modifyCategory(-1, Category.PROFESSIONAL)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `modifyCategory throws NOT_FOUND when contact does not exist`() {
        val response = modifyCategory(9999, Category.PROFESSIONAL)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `modifyCategory updates category and can be retrieved`() {
        val modResp = modifyCategory(1, Category.PROFESSIONAL)
        assertEquals(HttpStatus.OK, modResp.statusCode)

        val contact = getContact(1)
        assertEquals(Category.PROFESSIONAL, contact.category)
    }

    /** ------------------- addAddress ------------------------ **/
    private fun addAddress(contactId: Int, address: String): ResponseEntity<String> =
        restTemplate.postForEntity(
            "/API/contacts/$contactId/addresses",
            HttpEntity(address, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
            String::class.java
        )

    @Test
    fun `addAddress throws BAD_REQUEST when contactId is negative`() {
        val response = addAddress(-1, "Via Test 99")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `addAddress throws BAD_REQUEST when address is blank`() {
        val response = addAddress(1, "")
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `addAddress throws NOT_FOUND when contact does not exist`() {
        val response = addAddress(9999, "Via Test 99")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val json = mapper.readTree(response.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `addAddress appends new address and can be retrieved`() {
        val newAddr = "Via Napoli 10"
        val beforeSize = getContact(1).address.size
        val resp = addAddress(1, newAddr)
        assertEquals(HttpStatus.CREATED, resp.statusCode)
        val after = getContact(1)
        assertEquals(beforeSize + 1, after.address.size)
        assertTrue(after.address.any { it.address == newAddr })
    }

    @Test
    fun `addAddress reuses existing address and can be retrieved`() {
        val existingAddr = getContact(1).address.first().address
        val beforeSize = getContact(2).address.size
        val resp = addAddress(2, existingAddr)
        assertEquals(HttpStatus.CREATED, resp.statusCode)
        val after = getContact(2)
        assertEquals(beforeSize + 1, after.address.size)
        assertTrue(after.address.any { it.address == existingAddr })
    }

    /** ------------------- changeAddress ------------------------ **/
    private fun modifyAddress(contactId: Int, addressId: Int, address: String): ResponseEntity<String> =
        restTemplate.exchange(
            "/API/contacts/$contactId/address/$addressId",
            HttpMethod.PUT,
            HttpEntity(address, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
            String::class.java
        )

    @Test
    fun `modifyAddress updates unique address when single owner`() {
        val newAddr = "Via Torino 5"
        val resp = modifyAddress(1, 1, newAddr)
        assertEquals(HttpStatus.OK, resp.statusCode)
        val contact = getContact(1)
        assertTrue(contact.address.any { it.address == newAddr })
        assertFalse(contact.address.any { it.address == "Via Roma 1, Rome" })
    }

    @Test
    fun `modifyAddress duplicates and updates when address shared`() {
        val shared = "Via Roma 1, Rome"

        restTemplate.postForEntity(
            "/API/contacts/2/addresses",
            shared,
            String::class.java
        )

        val newAddr = "Via Torino 7"
        val resp = modifyAddress(2, 1, newAddr)
        assertEquals(HttpStatus.OK, resp.statusCode)

        val c1 = getContact(1)
        assertTrue(c1.address.any { it.address == "Via Roma 1, Rome" })

        val c2 = getContact(2)
        println(c2)
        assertTrue(c2.address.any { it.address == newAddr })
        assertFalse(c2.address.any { it.address == "Via Roma 1, Rome" })
    }

    /** ------------------- deleteAddress ------------------------ **/
    private fun deleteAddress(contactId: Int, addressId: Int): ResponseEntity<String> =
        restTemplate.exchange(
            "/API/contacts/$contactId/address/$addressId",
            HttpMethod.DELETE,
            HttpEntity(null, HttpHeaders()),
            String::class.java
        )

    @Test
    fun `deleteAddress throws BAD_REQUEST when contactId is negative`() {
        val resp = deleteAddress(-1, 1)
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        val json = mapper.readTree(resp.body!!)
        assertEquals("contactId must be >= 0", json["message"].asText())
    }

    @Test
    fun `deleteAddress throws BAD_REQUEST when addressId is negative`() {
        val resp = deleteAddress(1, -1)
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        val json = mapper.readTree(resp.body!!)
        assertEquals("addressId must be >= 0", json["message"].asText())
    }

    @Test
    fun `deleteAddress throws NOT_FOUND when contact does not exist`() {
        val resp = deleteAddress(9999, 1)
        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
        val json = mapper.readTree(resp.body!!)
        assertEquals("contact not found", json["message"].asText())
    }

    @Test
    fun `deleteAddress throws NOT_FOUND when address does not exist`() {
        val resp = deleteAddress(1, 9999)
        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
        val json = mapper.readTree(resp.body!!)
        assertEquals("address not found", json["message"].asText())
    }

    @Test
    fun `deleteAddress removes address and can be retrieved`() {
        addAddress(1, "Via Bologna 12")
        val before = getContact(1)
        val addrId = before.address.first().id
        val resp = deleteAddress(1, addrId)
        assertEquals(HttpStatus.OK, resp.statusCode)
        val after = getContact(1)
        assertFalse(after.address.any { it.id == addrId })
    }
}
