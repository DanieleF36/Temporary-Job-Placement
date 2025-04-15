package it.daniele.temporaryjobplacement.controllers

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank
import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.services.contact.ContactService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/API/contacts")
class ContactController(private val service: ContactService) {
    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") @Min(0, message = "Page number must be >= 0") page: Int,
        @RequestParam(defaultValue = "10") @Min(0, message = "Limit number must be > 0")limit: Int,
        @RequestParam sort: String?,
        @RequestParam(required = false) @OptionalNotBlank name: String?,
        @RequestParam(required = false) @OptionalNotBlank surname: String?,
        @RequestParam(required = false) @OptionalNotBlank email: String?,
        @RequestParam(required = false) @OptionalNotBlank telephone: String?,
    ): Page<ContactDTO>{
        val allowed = listOf("email", "name", "surname", "category")
        return service.getAll(page, limit, validateSort(allowed, sort, "email"), name, surname, email, telephone)
    }

    @GetMapping("/{contactId}")
    fun get(@PathVariable @Positive(message = "Id must be >= 0") contactId: Int): ContactDTO{
        val contact = service.get(contactId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "id not found")
        return contact
    }

    @PostMapping
    fun create(@Valid @RequestBody contactDTO: ContactDTO): ContactDTO = service.create(contactDTO)

    @PostMapping("/{contactId}/emails")
    fun addEmail(@PathVariable contactId: String, @RequestBody @NotBlank email: String): ContactDTO = service.addNewMail(contactId, email)
}