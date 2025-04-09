package it.daniele.temporaryjobplacement.controllers
import it.daniele.temporaryjobplacement.dtos.MessageDTO
import it.daniele.temporaryjobplacement.entities.message.State
import it.daniele.temporaryjobplacement.services.MessageService
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
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
}