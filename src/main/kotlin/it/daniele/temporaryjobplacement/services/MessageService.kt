package it.daniele.temporaryjobplacement.services
interface MessageService {
    fun getAll(page: Int, limit: Int, sort: Sort, state: State?): Page<MessageDTO>
    fun get(messageId: Int): MessageDTO?
}