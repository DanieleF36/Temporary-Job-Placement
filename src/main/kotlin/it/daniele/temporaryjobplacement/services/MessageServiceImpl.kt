package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.dtos.ActionDTO
import it.daniele.temporaryjobplacement.dtos.MessageDTO
import it.daniele.temporaryjobplacement.dtos.toDTO
import it.daniele.temporaryjobplacement.entities.message.Action
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.Message
import it.daniele.temporaryjobplacement.entities.message.State
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.exceptions.WrongNewStateException
import it.daniele.temporaryjobplacement.repositories.ActionRepository
import it.daniele.temporaryjobplacement.repositories.ContactRepository
import it.daniele.temporaryjobplacement.repositories.MessageRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull
import org.hibernate.Hibernate

@Service
@Transactional
class MessageServiceImpl(
    private val messageRepo: MessageRepository,
    private val contactRepository: ContactRepository,
    private val actionRepository: ActionRepository
): MessageService {
    override fun getAll(page: Int, limit: Int, sort: Sort, state: State?): Page<MessageDTO> {
        if (page < 0) throw IllegalArgumentException("Page must be >= 0")
        if (limit <= 0) throw IllegalArgumentException("Limit must be > 0")
        val pageable = PageRequest.of(page, limit, sort)
        if(state != null)
            return messageRepo.findByState(state, pageable).map { it.toDTO() }
        return messageRepo.findAll(pageable).map { it.toDTO() }
    }

    override fun get(messageId: Int): MessageDTO? {
        if (messageId <= 0) throw IllegalArgumentException("Message Id must be > 0")
        return messageRepo.findById(messageId).getOrNull()?.toDTO()
    }

    override fun create(senderId: Int, channel: Channel, subject: String?, body: String?, date: ZonedDateTime): MessageDTO {
        if (senderId <= 0) throw IllegalArgumentException("Sender Id must be > 0")
        val sender = contactRepository.findById(senderId).getOrNull() ?: throw NotFoundException("Sender not found $senderId")

        val message = Message(
            sender = sender,
            date = date,
            subject = subject,
            body = body,
            channel = channel,
            priority = 0,
            state = State.RECEIVED,
            actions = mutableListOf()
        )
        return messageRepo.save(message).toDTO()
    }

    override fun changeState(messageId: Int, newState: State, comment: String?): MessageDTO {
        if (messageId <= 0) throw IllegalArgumentException("Message Id must be > 0")
        val message = messageRepo.findById(messageId).getOrNull() ?: throw NotFoundException("Message id not found: $messageId")
        if(!message.state.checkNewState(newState))
            throw WrongNewStateException("$newState is not a valid new state for ${message.state}")
        message.state = newState
        val newAction = Action(
            message = message,
            state = newState,
            date = ZonedDateTime.now(),
            comment = comment
        )
        actionRepository.save(newAction)
        if (Hibernate.isInitialized(message.actions)) {
            message.actions.add(newAction)
        }
        return message.toDTO()
    }

    override fun getActionHistory(messageId: Int): List<ActionDTO> {
        if (messageId <= 0) throw IllegalArgumentException("Message Id must be > 0")
        val message = messageRepo.findById(messageId).getOrNull() ?: throw NotFoundException("Message not found $messageId")
        return message.actions.map { it.toDTO() }
    }

    override fun changePriority(messageId: Int, priority: Int): MessageDTO {
        if (messageId <= 0) throw IllegalArgumentException("Message Id must be > 0")
        if (priority < 0) throw IllegalArgumentException("Priority must be >= 0")
        val message = messageRepo.findById(messageId).getOrNull() ?: throw NotFoundException("Message not found $messageId")
        message.priority = priority
        return message.toDTO()
    }
}