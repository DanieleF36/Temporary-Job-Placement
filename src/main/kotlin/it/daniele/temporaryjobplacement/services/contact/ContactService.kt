package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.ContactDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface ContactService {
    fun getAll(page: Int, limit: Int, sort: Sort, name:String?, surname:String?, email:String?, telephone:String? ): Page<ContactDTO>
    fun get(contactId: Int): ContactDTO?

    fun create(contactDTO: ContactDTO): ContactDTO
}