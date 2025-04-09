package it.daniele.temporaryjobplacement.services

import it.daniele.temporaryjobplacement.dtos.ActionDTO
import it.daniele.temporaryjobplacement.dtos.MessageDTO
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.State
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import java.time.ZonedDateTime

interface MessageService {
    fun getAll(page: Int, limit: Int, sort: Sort, state: State?): Page<MessageDTO>
    fun get(messageId: Int): MessageDTO?
    fun create(senderId: Int, channel: Channel, subject: String?, body: String?, date: ZonedDateTime): MessageDTO
    fun changeState(messageId: Int, newState: State, comment: String?): MessageDTO
    fun getActionHistory(messageId: Int): List<ActionDTO>
    fun changePriority(messageId: Int, priority: Int): MessageDTO
}