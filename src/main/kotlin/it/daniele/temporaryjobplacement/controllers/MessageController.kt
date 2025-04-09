package it.daniele.temporaryjobplacement.controllers

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.dtos.ActionDTO
import it.daniele.temporaryjobplacement.dtos.MessageDTO
import it.daniele.temporaryjobplacement.entities.message.Channel
import it.daniele.temporaryjobplacement.entities.message.State
import it.daniele.temporaryjobplacement.services.MessageService
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime


@RestController
@RequestMapping("/API/messages")
class MessageController(private val service: MessageService) {
    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") @Min(0, message = "Page number must be >= 0") page: Int,
        @RequestParam(defaultValue = "10") @Positive(message = "Limit number must be > 0")limit: Int,
        @RequestParam sort: String?,
        @RequestParam(defaultValue = "received") filter: String?
    ): Page<MessageDTO> {
        val state = filter?.let {
            try {
                State.valueOf(it.uppercase())
            }catch (ex: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Filter is not a valid $filter")
            }
        }

        val allowedSort = listOf("sender", "date", "subject", "body", "channel", "priority", "state")
        return service.getAll(page, limit, validateSort(allowedSort, sort, "date"), state)
    }

    @GetMapping("/{messageId}")
    fun get(@PathVariable @Positive(message = "Message id must be > 0") messageId: Int): MessageDTO {
        return service.get(messageId) ?: throw throw ResponseStatusException(HttpStatus.NOT_FOUND, "id not found")
    }

    @PostMapping
    fun create(
        @RequestBody @Positive(message = "Sender id must be > 0")senderId: Int,
        @RequestBody channel: Channel,
        @RequestBody @OptionalNotBlank subject: String?,
        @RequestBody @OptionalNotBlank body: String?,
        @RequestBody date: ZonedDateTime
    ): MessageDTO {
        return service.create(senderId, channel, subject, body, date)
    }

    @PostMapping("/{messageId}")
    fun changeState(
        @PathVariable @Positive(message = "Message id must be > 0") messageId: Int,
        @RequestBody newState: State,
        @RequestBody @OptionalNotBlank comment: String?
    ): MessageDTO {
        //TODO forse questo va fatto nell'action service?
        return service.changeState(messageId, newState, comment)
    }

    @GetMapping("/{messageId}/history")
    fun getHistory(@PathVariable @Positive(message = "Message id must be > 0")messageId: Int): List<ActionDTO>{
        return service.getActionHistory(messageId)
    }
}