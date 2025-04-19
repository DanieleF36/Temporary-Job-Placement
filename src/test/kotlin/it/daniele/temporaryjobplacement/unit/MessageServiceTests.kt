package it.daniele.temporaryjobplacement.unit

import io.mockk.*
import it.daniele.temporaryjobplacement.dtos.message.toDTO
import it.daniele.temporaryjobplacement.dtos.toDTO
import it.daniele.temporaryjobplacement.services.MessageServiceImpl
import it.daniele.temporaryjobplacement.entities.message.Action
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import it.daniele.temporaryjobplacement.entities.contact.Contact
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.exceptions.WrongNewStateException
import it.daniele.temporaryjobplacement.repositories.ActionRepository
import it.daniele.temporaryjobplacement.repositories.ContactRepository
import it.daniele.temporaryjobplacement.repositories.MessageRepository
import org.hibernate.Hibernate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.util.ReflectionTestUtils
import java.time.ZonedDateTime
import java.util.Optional

internal class MessageServiceTests {

    private val messageRepo: MessageRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val actionRepository: ActionRepository = mockk()
    private val service = MessageServiceImpl(messageRepo, contactRepository, actionRepository)

    /** -------------------- getAll ----------------------- **/
    @Test
    fun `getAll throws IllegalArgumentException when page is negative`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.getAll(-1, 10, Sort.by(Sort.Direction.ASC, "id"), null)
        }
        assertEquals(exception.message, "Page must be >= 0")
    }

    @Test
    fun `getAll throws IllegalArgumentException when limit is less or equal to zero`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.getAll(0, 0, Sort.by(Sort.Direction.ASC, "id"), null)
        }
        assertEquals(exception.message, "Limit must be > 0")
    }

    @Test
    fun `getAll returns page of MessageDTO without state filter`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Test subject",
            body = "Test body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )

        val messageList = listOf(message)
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        val pageImpl: Page<Message> = PageImpl(messageList, pageable, 1)
        every { messageRepo.findAll(pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by(Sort.Direction.ASC, "id"), null)
        assertEquals(1, result.totalElements)
        assertEquals("Test subject", result.content[0].subject)
    }

    @Test
    fun `getAll returns page of MessageDTO with state filter`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Test subject",
            body = "Test body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )

        val messageList = listOf(message)
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        val pageImpl: Page<Message> = PageImpl(messageList, pageable, 1)
        every { messageRepo.findByState(State.RECEIVED, pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by(Sort.Direction.ASC, "id"), State.RECEIVED)
        assertEquals(1, result.totalElements)
        assertEquals("Test subject", result.content[0].subject)
    }

    @Test
    fun `getAll returns page of MessageDTO with state filter but no results`() {
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        val pageImpl: Page<Message> = PageImpl(emptyList(), pageable, 0)
        every { messageRepo.findByState(State.READ, pageable) } returns pageImpl

        val result = service.getAll(0, 10, Sort.by(Sort.Direction.ASC, "id"), State.READ)
        assertEquals(0, result.totalElements)
    }

    /** -------------------- get ----------------------- **/
    @Test
    fun `get throws IllegalArgumentException when messageId is negative`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.get(-1)
        }
        assertEquals(exception.message, "Message Id must be > 0")
    }

    @Test
    fun `get returns null when message not found`() {
        every { messageRepo.findById(1) } returns Optional.empty()

        val result = service.get(1)
        assertNull(result)
    }

    @Test
    fun `get returns MessageDTO when message is found`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Test",
            body = "Test body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        ReflectionTestUtils.setField(message, "id", 1)

        every { messageRepo.findById(1) } returns Optional.of(message)

        val result = service.get(1)
        assertNotNull(result)
        assertEquals("Test", result!!.subject)
    }

    /** -------------------- create ----------------------- **/
    @Test
    fun `create throws IllegalArgumentException when senderId is non-positive`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.create(0, Channel.EMAIL, "Subject", "Body", ZonedDateTime.now())
        }
        assertEquals(exception.message, "Sender Id must be > 0")
    }

    @Test
    fun `create throws NotFoundException when sender not found`() {
        every { contactRepository.findById(1) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.create(1, Channel.EMAIL, "Subject", "Body", ZonedDateTime.now())
        }
        assertEquals(exception.message, "Sender not found 1")
    }

    @Test
    fun `create returns MessageDTO when valid parameters provided`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val now = ZonedDateTime.now()
        val msg = Message(dummyContact, now, "Subject", "Body", Channel.EMAIL, 0, State.READ, mutableListOf())
        ReflectionTestUtils.setField(msg, "id", 1)

        every { contactRepository.findById(1) } returns Optional.of(dummyContact)
        every { messageRepo.save(any()) } answers { msg }


        val result = service.create(1, Channel.EMAIL, "Subject", "Body", now)
        assertNotNull(result)
        assertEquals(msg.toDTO(), result)
    }

    /** -------------------- changeState ----------------------- **/
    @Test
    fun `changeState throws IllegalArgumentException when messageId is non-positive`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.changeState(0, State.READ, "Comment")
        }
        assertEquals(exception.message, "Message Id must be > 0")
    }

    @Test
    fun `changeState throws NotFoundException when message not found`() {
        every { messageRepo.findById(1) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.changeState(1, State.READ, "Comment")
        }
        assertEquals(exception.message, "Message id not found: 1")
    }

    @Test
    fun `changeState throws WrongNewStateException when newState is not valid`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Subject",
            body = "Body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        ReflectionTestUtils.setField(message, "id", 1)

        mockkObject(message.state)
        every { message.state.checkNewState(State.DONE) } returns false
        every { messageRepo.findById(1) } returns Optional.of(message)

        val exception = assertThrows(WrongNewStateException::class.java) {
            service.changeState(1, State.DONE, "Comment")
        }
        assertEquals(exception.message, "${State.DONE} is not a valid new state for ${State.RECEIVED}")
        unmockkObject(message.state)
    }

    @Test
    fun `changeState returns MessageDTO when valid parameters provided`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val now = ZonedDateTime.now()
        val message = Message(
            sender = dummyContact,
            date = now,
            subject = "Subject",
            body = "Body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        ReflectionTestUtils.setField(message, "id", 1)

        mockkObject(message.state)
        every { message.state.checkNewState(State.READ) } returns true
        every { messageRepo.findById(1) } returns Optional.of(message)
        every { actionRepository.save(any()) } answers { firstArg() }

        val newAction = Action(message, State.READ, now, "comment")

        // Forzo Hibernate a inizializzare 'actions'
        mockkStatic(Hibernate::class)
        every { Hibernate.isInitialized(message.actions) } returns true

        val result = service.changeState(1, State.READ, "Comment")

        message.actions.add(newAction)
        assertNotNull(result)
        assertEquals(message.toDTO(), result)
        verify { actionRepository.save(match { it.comment == "Comment" && it.state == State.READ && it.message == message }) }

        unmockkStatic(Hibernate::class)
    }

    /** -------------------- getActionHistory ----------------------- **/
    @Test
    fun `getActionHistory throws IllegalArgumentException when messageId is non-positive`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.getActionHistory(0)
        }
        assertEquals(exception.message, "Message Id must be > 0")
    }

    @Test
    fun `getActionHistory throws NotFoundException when message not found`() {
        every { messageRepo.findById(1) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.getActionHistory(1)
        }
        assertEquals(exception.message, "Message not found 1")
    }

    @Test
    fun `getActionHistory returns list of ActionDTO when message is found`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Subject",
            body = "Body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        ReflectionTestUtils.setField(message, "id", 1)

        val action1 = Action(
            message = message,
            state = State.READ,
            date = ZonedDateTime.now(),
            comment = "Action 1"
        )
        message.actions.add(action1)

        every { messageRepo.findById(1) } returns Optional.of(message)

        val result = service.getActionHistory(1)
        assertNotNull(result)
        assertEquals(result, listOf(action1.toDTO()) )
        assertEquals("Action 1", result[0].comment)
    }

    /** -------------------- changePriority ----------------------- **/
    @Test
    fun `changePriority throws IllegalArgumentException when messageId is non-positive`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.changePriority(0, 1)
        }
        assertEquals(exception.message, "Message Id must be > 0")
    }

    @Test
    fun `changePriority throws IllegalArgumentException when priority is negative`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.changePriority(1, -1)
        }
        assertEquals(exception.message, "Priority must be >= 0")
    }

    @Test
    fun `changePriority throws NotFoundException when message not found`() {
        every { messageRepo.findById(1) } returns Optional.empty()

        val exception = assertThrows(NotFoundException::class.java) {
            service.changePriority(1, 1)
        }
        assertEquals(exception.message, "Message not found 1")
    }

    @Test
    fun `changePriority returns MessageDTO when valid parameters provided`() {
        val dummyContact = mockk<Contact>(relaxed = true)
        val message = Message(
            sender = dummyContact,
            date = ZonedDateTime.now(),
            subject = "Subject",
            body = "Body",
            channel = Channel.EMAIL,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        ReflectionTestUtils.setField(message, "id", 1)

        every { messageRepo.findById(1) } returns Optional.of(message)

        val result = service.changePriority(1, 5)
        assertNotNull(result)
        assertEquals(5, result.priority)
    }
}