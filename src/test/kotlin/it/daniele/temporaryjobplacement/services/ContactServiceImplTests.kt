package it.daniele.temporaryjobplacement.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.daniele.temporaryjobplacement.entities.contact.*
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
}