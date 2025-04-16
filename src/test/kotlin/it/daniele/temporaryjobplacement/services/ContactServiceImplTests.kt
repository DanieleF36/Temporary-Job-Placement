package it.daniele.temporaryjobplacement.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.entities.EntityBase
import it.daniele.temporaryjobplacement.entities.contact.*
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.repositories.AddressRepository
import it.daniele.temporaryjobplacement.repositories.ContactRepository
import it.daniele.temporaryjobplacement.repositories.EmailRepository
import it.daniele.temporaryjobplacement.repositories.TelephoneRepository
import it.daniele.temporaryjobplacement.services.contact.ContactServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*

internal class ContactServiceImplTests {
    private val contactRepo: ContactRepository = mockk()
    private val emailRepo: EmailRepository = mockk()
    private val addressRepo: AddressRepository = mockk()
    private val telephoneRepo: TelephoneRepository = mockk()

    private val service = ContactServiceImpl(contactRepo, emailRepo, addressRepo, telephoneRepo)

    private fun dummyContact(
        name: String = "John",
        surname: String = "Doe",
        ssn: String = "123",
        category: Category = Category.CUSTOMER
    ): Contact {
        return Contact(name, surname, mutableListOf(), mutableListOf(), mutableListOf(), ssn, category)
    }

    /** ------------------- getAll ------------------------ **/
    @Test
    fun `getAll throws IllegalArgumentException when page is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.getAll(-1, 10, Sort.by("name"), null, null, null, null)
        }
        assertEquals("Page must be >= 0", error.message)
    }

    @Test
    fun `getAll throws IllegalArgumentException when limit is non-positive`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.getAll(0, 0, Sort.by("name"), null, null, null, null)
        }
        assertEquals("Limit must be > 0", error.message)
    }

    @Test
    fun `getAll calls findAll when no filters provided`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact()
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findAll(pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by("name"), null, "  ", null, "")

        verify(exactly = 1) { contactRepo.findAll(pageable) }
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getAll with only name filter`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByNameContainsIgnoreCase("John", pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by("name"), "John", null, null, null)

        verify(exactly = 1) { contactRepo.findByNameContainsIgnoreCase("John", pageable) }
        assertEquals("John", result.content[0].name)
    }

    @Test
    fun `getAll with only surname filter`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findBySurnameContainsIgnoreCase("Doe", pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by("name"), null, "Doe", null, null)

        verify(exactly = 1) { contactRepo.findBySurnameContainsIgnoreCase("Doe", pageable) }
        assertEquals("Doe", result.content[0].surname)
    }

    @Test
    fun `getAll with only email filter`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact()
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByEmailContainsIgnoreCase("test@example.com", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, null, "test@example.com", null)
        verify(exactly = 1) { contactRepo.findByEmailContainsIgnoreCase("test@example.com", pageable) }
    }

    @Test
    fun `getAll with only telephone filter`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact()
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByTelephoneContainsIgnoreCase("1234567890", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, null, null, "1234567890")
        verify(exactly = 1) { contactRepo.findByTelephoneContainsIgnoreCase("1234567890", pageable) }
    }

    @Test
    fun `getAll with name and surname filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John", surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCase("John", "Doe", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", "Doe", null, null)
        verify(exactly = 1) { contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCase("John", "Doe", pageable) }
    }

    @Test
    fun `getAll with email and telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact()
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("test@example.com", "1234567890", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, null, "test@example.com", "1234567890")
        verify(exactly = 1) { contactRepo.findByEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("test@example.com", "1234567890", pageable) }
    }

    @Test
    fun `getAll with name and email filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCase("John", "test@example.com", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", null, "test@example.com", null)
        verify(exactly = 1) { contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCase("John", "test@example.com", pageable) }
    }

    @Test
    fun `getAll with name and telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findByNameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "1234567890", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", null, null, "1234567890")
        verify(exactly = 1) { contactRepo.findByNameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "1234567890", pageable) }
    }

    @Test
    fun `getAll with surname and email filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCase("Doe", "test@example.com", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, "Doe", "test@example.com", null)
        verify(exactly = 1) { contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCase("Doe", "test@example.com", pageable) }
    }

    @Test
    fun `getAll with surname and telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every { contactRepo.findBySurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("Doe", "1234567890", pageable) } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, "Doe", null, "1234567890")
        verify(exactly = 1) { contactRepo.findBySurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("Doe", "1234567890", pageable) }
    }

    @Test
    fun `getAll with name, surname, email filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John", surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrEmailContainsIgnoreCase("John", "Doe", "test@example.com", pageable)
        } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", "Doe", "test@example.com", null)
        verify(exactly = 1) {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrEmailContainsIgnoreCase("John", "Doe", "test@example.com", pageable)
        }
    }

    @Test
    fun `getAll with name, surname, telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John", surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "Doe", "1234567890", pageable)
        } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", "Doe", null, "1234567890")
        verify(exactly = 1) {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "Doe", "1234567890", pageable)
        }
    }

    @Test
    fun `getAll with name, email, telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every {
            contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "test@example.com", "1234567890", pageable)
        } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", null, "test@example.com", "1234567890")
        verify(exactly = 1) {
            contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("John", "test@example.com", "1234567890", pageable)
        }
    }

    @Test
    fun `getAll with surname, email, telephone filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every {
            contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("Doe", "test@example.com", "1234567890", pageable)
        } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), null, "Doe", "test@example.com", "1234567890")
        verify(exactly = 1) {
            contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase("Doe", "test@example.com", "1234567890", pageable)
        }
    }

    @Test
    fun `getAll with all filters`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val contact = dummyContact(name = "John", surname = "Doe")
        val pageImpl = PageImpl(listOf(contact), pageable, 1)
        every {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCaseOrEmailContainsIgnoreCase(
                "John", "Doe", "1234567890", "test@example.com", pageable
            )
        } returns pageImpl

        service.getAll(0, 10, Sort.by("name"), "John", "Doe", "test@example.com", "1234567890")
        verify(exactly = 1) {
            contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCaseOrEmailContainsIgnoreCase(
                "John", "Doe", "1234567890", "test@example.com", pageable
            )
        }
    }

    /** --------------------- get ------------------------- **/
    @Test
    fun `get throws IllegalArgumentException for negative id`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.get(-1) }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `get returns null when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val result = service.get(1)
        assertNull(result)
    }

    @Test
    fun `get returns contact DTO when found`() {
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        val result = service.get(1)
        assertNotNull(result)
        assertEquals(contact.name, result!!.name)
    }

    /** --------------------- create ------------------------- **/
    @Test
    fun `create creates contact with new email, address, and telephone when entities do not exist`() {
        val contactDTO = ContactDTO(
            name = "Alice",
            surname = "Smith",
            email = listOf("alice@example.com"),
            address = listOf("123 Main St"),
            telephone = listOf("3912345678"),
            ssn = "111-22-3333",
            category = Category.CUSTOMER
        )

        every { emailRepo.findByEmail("alice@example.com") } returns mutableListOf()
        every { emailRepo.save(any<Email>()) } answers { firstArg<Email>() }

        every { addressRepo.findByAddress("123 Main St") } returns mutableListOf()
        every { addressRepo.save(any<Address>()) } answers { firstArg<Address>() }

        every { telephoneRepo.findByPrefixAndNumber(39, 12345678) } returns mutableListOf()
        every { telephoneRepo.save(any<Telephone>()) } answers { firstArg<Telephone>() }

        every { contactRepo.save(any<Contact>()) } answers { firstArg<Contact>() }

        val result = service.create(contactDTO)
        assertEquals("Alice", result.name)
        assertEquals("Smith", result.surname)
        assertEquals("111-22-3333", result.ssn)
        assertEquals(Category.CUSTOMER, result.category)
        assertEquals(1, result.email.size)
        assertEquals("alice@example.com", result.email[0])
        assertEquals(1, result.address.size)
        assertEquals("123 Main St", result.address[0])
        assertEquals(1, result.telephone.size)
    }

    @Test
    fun `create reuses existing email, address, and telephone when available`() {
        val contactDTO = ContactDTO(
            name = "Bob",
            surname = "Johnson",
            email = listOf("bob@example.com"),
            address = listOf("456 Elm St"),
            telephone = listOf("4012345678"),
            ssn = "222-33-4444",
            category = Category.CUSTOMER
        )

        val existingEmail = Email("bob@example.com", mutableListOf())
        val existingAddress = Address("456 Elm St", emptyList())
        val existingTelephone = Telephone(40, 12345678, mutableListOf())

        every { emailRepo.findByEmail("bob@example.com") } returns mutableListOf(existingEmail)
        every { addressRepo.findByAddress("456 Elm St") } returns mutableListOf(existingAddress)
        every { telephoneRepo.findByPrefixAndNumber(40, 12345678) } returns mutableListOf(existingTelephone)
        every { contactRepo.save(any<Contact>()) } answers { firstArg<Contact>() }

        val result = service.create(contactDTO)
        assertEquals("Bob", result.name)
        assertEquals("Johnson", result.surname)
        assertEquals("222-33-4444", result.ssn)
        assertEquals(Category.CUSTOMER, result.category)
        assertEquals(1, result.email.size)
        assertEquals("bob@example.com", result.email[0])
        assertEquals(1, result.address.size)
        assertEquals("456 Elm St", result.address[0])
        assertEquals(1, result.telephone.size)
    }


    /** --------------------- delete ------------------------- **/
    @Test
    fun `delete throws IllegalArgumentException when contactId is non-positive`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.delete(0) }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `delete throws NotFoundException when contact not found`() {
        every { contactRepo.existsById(1) } returns false
        val error = assertThrows(NotFoundException::class.java) { service.delete(1) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `delete calls deleteById when contact exists`() {
        every { contactRepo.existsById(1) } returns true
        every { contactRepo.findById(1) } returns Optional.of(dummyContact())
        every { contactRepo.deleteById(1) } returns Unit

        service.delete(1)
        verify(exactly = 1) { contactRepo.deleteById(1) }
    }

    /** --------------------- addNewEmail ------------------------- **/
    @Test
    fun `addNewEmail throws IllegalArgumentException when contactId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.addNewEmail(-1, "new@example.com") }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `addNewEmail throws IllegalArgumentException when email is blank`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.addNewEmail(1, "   ") }
        assertEquals("email must be not blank", error.message)
    }

    @Test
    fun `addNewEmail throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.addNewEmail(1, "new@example.com") }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `addNewEmail adds email to contact when email exists in repository`() {
        val contact = dummyContact()
        contact.email.clear()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        val existingEmail = Email("new@example.com", mutableListOf())
        every { emailRepo.findByEmail("new@example.com") } returns mutableListOf(existingEmail)

        val result = service.addNewEmail(1, "new@example.com")
        assertTrue(result.email.contains("new@example.com"))
    }

    @Test
    fun `addNewEmail adds email to contact when email does not exists in repository`() {
        val contact = dummyContact()
        contact.email.clear()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        val existingEmail = Email("new@example.com", mutableListOf())
        every { emailRepo.findByEmail("new@example.com") } returns mutableListOf()
        every { emailRepo.save(existingEmail) } returns existingEmail
        val result = service.addNewEmail(1, "new@example.com")
        assertTrue(result.email.contains("new@example.com"))
    }

    /** --------------------- deleteEmail ------------------------- **/
    @Test
    fun `deleteEmail throws IllegalArgumentException when contactId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.deleteEmail(0, 1) }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `deleteEmail throws IllegalArgumentException when emailId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.deleteEmail(1, 0) }
        assertEquals("emailId must be > 0", error.message)
    }

    @Test
    fun `deleteEmail throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.deleteEmail(1, 1) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `deleteEmail throws NotFoundException when email not found`() {
        val contact = dummyContact()
        contact.email.add(Email("test@example.com", mutableListOf()))
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { emailRepo.findById(1) } returns Optional.empty()

        val error = assertThrows(NotFoundException::class.java) { service.deleteEmail(1, 1) }
        assertEquals("email not found", error.message)
    }

    @Test
    fun `deleteEmail removes email from contact and deletes email when no contacts remain`() {
        val email = Email("test@example.com", mutableListOf())
        val contact = dummyContact()
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(contact, 1)
            }
        email.contact.add(contact)
        contact.email.add(email)
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { emailRepo.findById(1) } returns Optional.of(email)
        every { emailRepo.deleteById(1) } returns Unit

        service.deleteEmail(1, 1)
        assertFalse(contact.email.contains(email))
        assertTrue(contact.email.isEmpty())
        verify(exactly = 1) { emailRepo.deleteById(1) }
    }

    @Test
    fun `deleteEmail removes email from contact without deleting email when contacts remain`() {
        val email = Email("test@example.com", mutableListOf())
        email.contact.add(dummyContact())
        email.contact.add(dummyContact())
        val contact = dummyContact()
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(contact, 1)
            }
        contact.email.add(email)
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { emailRepo.findById(1) } returns Optional.of(email)

        service.deleteEmail(1, 1)
        assertFalse(contact.email.contains(email))
        verify(exactly = 0) { emailRepo.deleteById(1) }
    }

    /** --------------------- changeCategory ------------------------- **/
    @Test
    fun `changeCategory throws IllegalArgumentException for non-positive contactId`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.changeCategory(0, Category.CUSTOMER) }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `changeCategory throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.changeCategory(1, Category.CUSTOMER) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `changeCategory updates category and returns updated DTO`() {
        val contact = dummyContact(category = Category.CUSTOMER)
        every { contactRepo.findById(1) } returns Optional.of(contact)

        val result = service.changeCategory(1, Category.CUSTOMER)
        assertEquals(Category.CUSTOMER, result.category)
    }
}