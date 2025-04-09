package it.daniele.temporaryjobplacement.services
import it.daniele.temporaryjobplacement.dtos.MessageDTO
import it.daniele.temporaryjobplacement.dtos.toDTO
import it.daniele.temporaryjobplacement.entities.message.State
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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