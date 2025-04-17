package it.daniele.temporaryjobplacement.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.dtos.TelephoneDTO
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

    /** --------------------- update ------------------------- **/
    @Test
    fun `update throws IllegalArgumentException for negative id`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.update(-1, "A", "B", "C")
        }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `update throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) {
            service.update(1, "A", "B", "C")
        }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `update changes name, surname and ssn of existing contact`() {
        val contact = dummyContact(name = "Old", surname = "Name", ssn = "000")
        every { contactRepo.findById(1) } returns Optional.of(contact)

        val result = service.update(1, "NewName", "NewSurname", "999")

        assertEquals("NewName", result.name)
        assertEquals("NewSurname", result.surname)
        assertEquals("999", result.ssn)
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
        val existingAddress = Address("456 Elm St", mutableListOf())
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

    /** --------------------- changeEmail ------------------------- **/
    @Test
    fun `changeEmail throws IllegalArgumentException for non-positive contactId`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.changeEmail(0, 1, "new@example.com")
        }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `changeEmail throws IllegalArgumentException for non-positive emailId`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.changeEmail(1, 0, "new@example.com")
        }
        assertEquals("emailId must be > 0", error.message)
    }

    @Test
    fun `changeEmail throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) {
            service.changeEmail(1, 1, "new@example.com")
        }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `changeEmail throws NotFoundException when email not found`() {
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { emailRepo.findById(1) } returns Optional.empty()

        val error = assertThrows(NotFoundException::class.java) {
            service.changeEmail(1, 1, "new@example.com")
        }
        assertEquals("email not found", error.message)
    }

    @Test
    fun `changeEmail updates email when singly associated`() {
        val contact = dummyContact()
        contact.email.clear()
        val email = Email("old@example.com", mutableListOf(contact))
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(contact, 1)
        }
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(email, 1)
        }
        contact.email.add(email)

        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { emailRepo.findById(1) } returns Optional.of(email)

        val result = service.changeEmail(1, 1, "new@example.com")

        assertTrue(result.email.contains("new@example.com"))
        assertFalse(result.email.contains("old@example.com"))
    }

    @Test
    fun `changeEmail creates new email when shared by multiple contacts`() {
        val contact1 = dummyContact()
        val contact2 = dummyContact(name = "Other")
                EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(contact1, 1)
        }
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(contact2, 2)
        }
        val shared = Email("shared@example.com", mutableListOf(contact1, contact2))
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(shared, 1)
        }
        contact1.email.add(shared)
        contact2.email.add(shared)

        every { contactRepo.findById(1) } returns Optional.of(contact1)
        every { emailRepo.findById(1) } returns Optional.of(shared)
        every { emailRepo.save(any<Email>()) } answers { firstArg<Email>() }

        val result = service.changeEmail(1, 1, "new@example.com")

        // original shared remains for contact2
        assertTrue(contact2.email.any { it.email == "shared@example.com" })
        // contact1 now has only the new email
        assertTrue(result.email.contains("new@example.com"))
        verify(exactly = 1) { emailRepo.save(any()) }
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

    /** --------------------- addAddress ------------------------- **/
    @Test
    fun `addAddress throws IllegalArgumentException when contactId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.addAddress(-1, "123 Main St")
        }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `addAddress throws IllegalArgumentException when address is blank`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.addAddress(1, "   ")
        }
        assertEquals("address must be not blank", error.message)
    }

    @Test
    fun `addAddress throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) {
            service.addAddress(1, "123 Main St")
        }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `addAddress creates new address when not present`() {
        val contact = dummyContact()
        contact.address.clear()

        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findByAddress("123 Main St") } returns mutableListOf()
        every { addressRepo.save(any<Address>()) } answers { firstArg<Address>() }

        val result = service.addAddress(1, "123 Main St")
        assertTrue(result.address.contains("123 Main St"))
    }

    @Test
    fun `addAddress reuses existing address when available`() {
        val contact = dummyContact()
        val existing = Address("456 Elm St", mutableListOf(contact))

        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findByAddress("456 Elm St") } returns mutableListOf(existing)

        val result = service.addAddress(1, "456 Elm St")
        assertTrue(result.address.contains("456 Elm St"))
        verify(exactly = 0) { addressRepo.save(any()) }
    }


    /** --------------------- changeAddress ------------------------- **/
    @Test
    fun `changeAddress throws IllegalArgumentException for non-positive contactId`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.changeAddress(-1, 2, "") }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `changeAddress throws IllegalArgumentException for non-positive addressId`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.changeAddress(1, 0, "") }
        assertEquals("addressId must be > 0", error.message)
    }

    @Test
    fun `changeAddress throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.changeAddress(1, 2, "") }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `changeAddress throws NotFoundException when address not found`() {
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findById(2) } returns Optional.empty()

        val error = assertThrows(NotFoundException::class.java) { service.changeAddress(1, 2, "") }
        assertEquals("address not found", error.message)
    }

    @Test
    fun `changeAddress adds new address when not present`() {
        val contact = dummyContact()
        val existing = Address(address = "789 Oak St", contact = mutableListOf(contact))
        contact.address.add(existing)

        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findById(2) } returns Optional.of(existing)

        val dto = service.changeAddress(1, 2, "788 Oak St")

        assertTrue(dto.address.any { it == "788 Oak St" })
        assertEquals(1, dto.address.size)
        verify(exactly = 0) { addressRepo.save(any()) }
    }

    @Test
    fun `changeAddress does not add address`() {
        val contact1 = dummyContact()
        val contact2 = dummyContact(name = "Dan")
        val shared = Address(address = "789 Oak St", contact = mutableListOf(contact1, contact2))
        contact1.address.add(shared)
        contact2.address.add(shared)

        every { contactRepo.findById(1) } returns Optional.of(contact1)
        every { addressRepo.findById(2) } returns Optional.of(shared)

        val saved = Address(address = "788 Oak St", contact = mutableListOf(contact1))
        every { addressRepo.save(Address("788 Oak St", mutableListOf(contact1)))} returns saved

        val dto = service.changeAddress(1, 2, "788 Oak St")

        verify(exactly = 1) { addressRepo.save(saved) }
        assertTrue(dto.address.any { it == "788 Oak St" })
        assertTrue(contact2.address.any { it.address == "789 Oak St" })
    }

    /** --------------------- deleteAddress ------------------------- **/
    @Test
    fun `deleteAddress throws IllegalArgumentException when contactId is non-positive`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.deleteAddress(0, 1)
        }
        assertEquals("contactId must be > 0", error.message)
    }

    @Test
    fun `deleteAddress throws IllegalArgumentException when addressId is non-positive`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.deleteAddress(1, 0)
        }
        assertEquals("addressId must be > 0", error.message)
    }

    @Test
    fun `deleteAddress throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) {
            service.deleteAddress(1, 1)
        }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `deleteAddress throws NotFoundException when address not found`() {
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findById(2) } returns Optional.empty()

        val error = assertThrows(NotFoundException::class.java) {
            service.deleteAddress(1, 2)
        }
        assertEquals("address not found", error.message)
    }

    @Test
    fun `deleteAddress removes address and deletes when no contacts remain`() {
        val contact = dummyContact()
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true; set(contact, 1)
        }
        val addr = Address("789 Oak St", mutableListOf(contact))
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true; set(addr, 2)
        }
        contact.address.add(addr)

        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { addressRepo.findById(2) } returns Optional.of(addr)
        every { addressRepo.deleteById(2) } returns Unit

        service.deleteAddress(1, 2)
        assertFalse(contact.address.contains(addr))
        verify(exactly = 1) { addressRepo.deleteById(2) }
    }

    @Test
    fun `deleteAddress removes address without deleting when contacts remain`() {
        val contact1 = dummyContact()
        val contact2 = dummyContact(name = "Dan")
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true; set(contact1, 1)
        }
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true; set(contact2, 2)
        }
        val shared = Address("789 Oak St", mutableListOf(contact1, contact2))
        EntityBase::class.java.getDeclaredField("id").apply {
            isAccessible = true; set(shared, 2)
        }
        contact1.address.add(shared)
        contact2.address.add(shared)

        every { contactRepo.findById(1) } returns Optional.of(contact1)
        every { addressRepo.findById(2) } returns Optional.of(shared)

        service.deleteAddress(1, 2)
        assertFalse(contact1.address.contains(shared))
        verify(exactly = 0) { addressRepo.deleteById(2) }
    }

    /** --------------------- addTelephone ------------------------- **/
    @Test
    fun `addTelephone throws IllegalArgumentException when contactId is negative`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 12345678)
        val error = assertThrows(IllegalArgumentException::class.java) { service.addTelephone(-1, telDTO) }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `addTelephone throws NotFoundException when contact not found`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 12345678)
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.addTelephone(1, telDTO) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `addTelephone saves telephone and adds it to contact`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 12345678)
        val contact = dummyContact()
        contact.telephone.clear()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.save(any<Telephone>()) } answers { firstArg<Telephone>() }

        val result = service.addTelephone(1, telDTO)
        assertTrue(result.telephone.isNotEmpty())
    }

    /** --------------------- changeTelephone ------------------------- **/
    @Test
    fun `changeTelephone throws IllegalArgumentException when contactId is negative`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 87654321)
        val error = assertThrows(IllegalArgumentException::class.java) { service.changeTelephone(-1, 1, telDTO) }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `changeTelephone throws IllegalArgumentException when phoneId is negative`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 87654321)
        val error = assertThrows(IllegalArgumentException::class.java) { service.changeTelephone(1, -1, telDTO) }
        assertEquals("phoneId must be >= 0", error.message)
    }

    @Test
    fun `changeTelephone throws NotFoundException when contact not found`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 87654321)
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.changeTelephone(1, 1, telDTO) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `changeTelephone throws NotFoundException when telephone not found`() {
        val telDTO = TelephoneDTO(prefix = 39, number = 87654321)
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.changeTelephone(1, 1, telDTO) }
        assertEquals("telephone not found", error.message)
    }

    @Test
    fun `changeTelephone adds new telephone when not present`() {
        val contact = dummyContact()
        val existingTel = Telephone(prefix = 39, number = 123456, contact = mutableListOf(contact))
        contact.telephone.add(existingTel)
        val dtoInput = TelephoneDTO(prefix = 39, number = 654321)
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.findById(2) } returns Optional.of(existingTel)

        val resultDto = service.changeTelephone(1, 2, dtoInput)

        assertEquals(39, existingTel.prefix)
        assertEquals(654321, existingTel.number)
        assertEquals(1, resultDto.telephone.size)
        val telDto = resultDto.telephone[0]
        assertEquals("39654321", telDto)
        verify(exactly = 0) { telephoneRepo.save(any()) }
    }

    @Test
    fun `changeTelephone does not adds new telephone when present`(){
        val contact1 = dummyContact()
        val contact2 = dummyContact()
        val sharedTel = Telephone(prefix = 39, number = 123456, contact = mutableListOf(contact1, contact2))
        contact1.telephone.add(sharedTel)
        contact2.telephone.add(sharedTel)
        val dtoInput = TelephoneDTO(prefix = 44, number = 777888)

        every { contactRepo.findById(1) } returns Optional.of(contact1)
        every { telephoneRepo.findById(2) } returns Optional.of(sharedTel)

        val savedTel = Telephone(prefix = 44, number = 777888, contact = mutableListOf(contact1))
        every { telephoneRepo.save(Telephone(prefix = 44, number = 777888, contact = mutableListOf(contact1)))} returns savedTel

        val resultDto = service.changeTelephone(1, 2, dtoInput)

        verify(exactly = 1) { telephoneRepo.save(any()) }
        assertTrue(resultDto.telephone.any { it == "44777888" })
        assertTrue(contact2.telephone.any { it == sharedTel })
    }

    /** --------------------- deleteTelephone ------------------------- **/
    @Test
    fun `deleteTelephone throws IllegalArgumentException when contactId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.deleteTelephone(-1, 1) }
        assertEquals("id must be >= 0", error.message)
    }

    @Test
    fun `deleteTelephone throws IllegalArgumentException when phoneId is negative`() {
        val error = assertThrows(IllegalArgumentException::class.java) { service.deleteTelephone(1, -1) }
        assertEquals("phoneId must be >= 0", error.message)
    }

    @Test
    fun `deleteTelephone throws NotFoundException when contact not found`() {
        every { contactRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.deleteTelephone(1, 1) }
        assertEquals("contact not found", error.message)
    }

    @Test
    fun `deleteTelephone throws NotFoundException when telephone not found`() {
        val contact = dummyContact()
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.findById(1) } returns Optional.empty()
        val error = assertThrows(NotFoundException::class.java) { service.deleteTelephone(1, 1) }
        assertEquals("telephone not found", error.message)
    }

    @Test
    fun `deleteTelephone removes telephone and calls removeById when no contacts remain`() {
        val telephone = Telephone(39, 12345678, mutableListOf())
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(telephone, 1)
            }
        val contact = dummyContact()
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(contact, 1)
            }

        telephone.contact.add(contact)
        contact.telephone.add(telephone)
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.findById(1) } returns Optional.of(telephone)
        every { telephoneRepo.removeById(1) } returns Unit

        service.deleteTelephone(1, 1)
        assertFalse(contact.telephone.contains(telephone))
        verify(exactly = 1) { telephoneRepo.removeById(1) }
    }

    @Test
    fun `deleteTelephone removes telephone without calling removeById when contacts remain`() {
        val contact0 = dummyContact()
        val contact = dummyContact()
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(contact, 1)
            }
        val telephone = Telephone(39, 12345678, mutableListOf())
        EntityBase::class.java
            .getDeclaredField("id")
            .apply {
                isAccessible = true
                set(telephone, 1)
            }
        telephone.contact.add(contact0)
        telephone.contact.add(contact)
        contact.telephone.add(telephone)
        every { contactRepo.findById(1) } returns Optional.of(contact)
        every { telephoneRepo.findById(1) } returns Optional.of(telephone)
        every { telephoneRepo.removeById(1) } returns Unit

        service.deleteTelephone(1, 1)
        assertFalse(contact.telephone.contains(telephone))
        verify(exactly = 0) { telephoneRepo.removeById(1) }
    }
}