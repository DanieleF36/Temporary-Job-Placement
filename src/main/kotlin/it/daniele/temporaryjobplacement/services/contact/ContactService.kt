package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.entities.contact.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface ContactService {
    fun getAll(page: Int, limit: Int, sort: Sort, name:String?, surname:String?, email:String?, telephone:String? ): Page<ContactDTO>
    fun get(contactId: Int): ContactDTO?

    fun create(contactDTO: ContactDTO): ContactDTO

    fun addNewEmail(contactId: Int, email: String): ContactDTO
    fun deleteEmail(contactId: Int, emailId: Int)
    fun changeCategory(contactId: Int, category: Category): ContactDTO
    fun changeAddress(contactId: Int, addressId: Int): ContactDTO
}