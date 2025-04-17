package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.dtos.TelephoneDTO
import it.daniele.temporaryjobplacement.entities.contact.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface ContactService {
    fun getAll(page: Int, limit: Int, sort: Sort, name:String?, surname:String?, email:String?, telephone:String? ): Page<ContactDTO>
    fun get(contactId: Int): ContactDTO?
    fun update(contactId: Int, name: String?, surname: String?, ssn: String?): ContactDTO
    fun create(contactDTO: ContactDTO): ContactDTO
    fun delete(contactId: Int)

    fun changeEmail(contactId: Int, emailId: Int, email: String): ContactDTO
    fun addNewEmail(contactId: Int, email: String): ContactDTO
    fun deleteEmail(contactId: Int, emailId: Int)

    fun changeCategory(contactId: Int, category: Category): ContactDTO

    fun addAddress(contactId: Int, address: String): ContactDTO
    fun changeAddress(contactId: Int, addressId: Int, address: String): ContactDTO
    fun deleteAddress(contactId: Int, addressId: Int)

    fun addTelephone(contactId: Int, telephoneDTO: TelephoneDTO): ContactDTO
    fun changeTelephone(contactId: Int, phoneId: Int, telephoneDTO: TelephoneDTO): ContactDTO
    fun deleteTelephone(contactId: Int, phoneId: Int): ContactDTO
}