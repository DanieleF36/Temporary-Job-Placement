package it.daniele.temporaryjobplacement.controllers
@RestController
@RequestMapping("/API/messages")
class MessageController(private val service: MessageService) {
}