package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.contact.ContactDTO
import it.daniele.temporaryjobplacement.dtos.contact.TelephoneDTO
import it.daniele.temporaryjobplacement.dtos.contact.toDTO
import it.daniele.temporaryjobplacement.entities.contact.*
import it.daniele.temporaryjobplacement.exceptions.NotFoundException
import it.daniele.temporaryjobplacement.repositories.AddressRepository
import it.daniele.temporaryjobplacement.repositories.ContactRepository
import it.daniele.temporaryjobplacement.repositories.EmailRepository
import it.daniele.temporaryjobplacement.repositories.TelephoneRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ContactServiceImpl(
    private val contactRepo: ContactRepository,
    private val emailRepository: EmailRepository,
    private val addressRepository: AddressRepository,
    private val telephoneRepository: TelephoneRepository
): ContactService {
    override fun getAll(page: Int, limit: Int, sort: Sort, name: String?, surname: String?, email: String?, telephone: String?): Page<ContactDTO> {
        if (page < 0) throw IllegalArgumentException("Page must be >= 0")
        if (limit <= 0) throw IllegalArgumentException("Limit must be > 0")

        val pageable = PageRequest.of(page, limit, sort)

        val hasName = !name.isNullOrBlank()
        val hasSurname = !surname.isNullOrBlank()
        val hasEmail = !email.isNullOrBlank()
        val hasTelephone = !telephone.isNullOrBlank()

        val page = when {
            !hasName && !hasSurname && !hasEmail && !hasTelephone ->
                contactRepo.findAll(pageable)

            // Single filter cases:
            hasName && !hasSurname && !hasEmail && !hasTelephone ->
                contactRepo.findByNameContainsIgnoreCase(name!!, pageable)
            !hasName && hasSurname && !hasEmail && !hasTelephone ->
                contactRepo.findBySurnameContainsIgnoreCase(surname!!, pageable)
            !hasName && !hasSurname && hasEmail && !hasTelephone ->
                contactRepo.findByEmailContainsIgnoreCase(email!!, pageable)
            !hasName && !hasSurname && !hasEmail && hasTelephone ->
                contactRepo.findByTelephoneContainsIgnoreCase(telephone!!, pageable)

            // Two filter cases:
            hasName && hasSurname && !hasEmail && !hasTelephone ->
                contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCase(name!!, surname!!, pageable)
            hasEmail && hasTelephone && !hasName && !hasSurname ->
                contactRepo.findByEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(email!!, telephone!!, pageable)
            hasName && hasEmail && !hasSurname && !hasTelephone ->
                contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCase(name!!, email!!, pageable)
            hasName && hasTelephone && !hasSurname && !hasEmail ->
                contactRepo.findByNameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name!!, telephone!!, pageable)
            hasSurname && hasEmail && !hasName && !hasTelephone ->
                contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCase(surname!!, email!!, pageable)
            hasSurname && hasTelephone && !hasName && !hasEmail ->
                contactRepo.findBySurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(surname!!, telephone!!, pageable)

            // Three filter cases:
            hasName && hasSurname && hasEmail && !hasTelephone ->
                contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrEmailContainsIgnoreCase(name!!, surname!!, email!!, pageable)
            hasName && hasSurname && hasTelephone && !hasEmail ->
                contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name!!, surname!!, telephone!!, pageable)
            hasName && hasEmail && hasTelephone && !hasSurname ->
                contactRepo.findByNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(name!!, email!!, telephone!!, pageable)
            hasSurname && hasEmail && hasTelephone && !hasName ->
                contactRepo.findBySurnameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrTelephoneContainsIgnoreCase(surname!!, email!!, telephone!!, pageable)

            // Four filter case: all filters provided
            hasName && hasSurname && hasEmail && hasTelephone ->
                contactRepo.findByNameContainsIgnoreCaseOrSurnameContainsIgnoreCaseOrTelephoneContainsIgnoreCaseOrEmailContainsIgnoreCase(
                    name!!, surname!!, telephone!!, email!!, pageable
                )

            else -> contactRepo.findAll(pageable)
        }
        return page.map { it.toDTO() }
    }

    override fun get(contactId: Int): ContactDTO? {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        return contactRepo.findById(contactId).getOrNull()?.toDTO()
    }

    override fun update(contactId: Int, name: String?, surname: String?, ssn: String?): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        if(name != null)
            contact.name = name
        if(surname != null)
            contact.surname = surname
        if(ssn != null)
            contact.ssn = ssn
        return contact.toDTO()
    }

    override fun create(contactDTO: ContactDTO): ContactDTO {
        val emailEntity = mutableListOf<Email>()
        contactDTO.email.forEach { emailDTO ->
            val emails = emailRepository.findByEmail(emailDTO.email)
            val email = if(emails.size == 0)
                emailRepository.save(Email(emailDTO.email, mutableListOf()))
            else
                emails[0]
            emailEntity.add(email)
        }

        val addressEntity = mutableListOf<Address>()
        contactDTO.address.forEach { addressDTO ->
            val adds = addressRepository.findByAddress(addressDTO.address)
            val add = if(adds.size == 0)
                addressRepository.save(Address(addressDTO.address, mutableListOf()))
            else
                adds[0]
            addressEntity.add(add)
        }

        val telephoneEntity = mutableListOf<Telephone>()
        contactDTO.telephone.forEach { telDTO ->
            val telStr = "${telDTO.prefix}${telDTO.number}"
            val str = if(telStr.startsWith("+"))
                telStr.drop(1)
            else telStr
            val prefix = str.subSequence(0, 2).toString()
            val number = str.subSequence(2, telStr.length).toString()
            val tels = telephoneRepository.findByPrefixAndNumber(prefix, number)
            val tel = if(tels.size == 0)
                telephoneRepository.save(Telephone(prefix, number, mutableListOf()))
            else
                tels[0]
            telephoneEntity.add(tel)
        }

        val contact = Contact(
            name = contactDTO.name,
            surname = contactDTO.surname,
            email = emailEntity,
            address = addressEntity,
            telephone = telephoneEntity,
            ssn = contactDTO.ssn,
            category = contactDTO.category
        )

        return contactRepo.save(contact).toDTO()
    }

    override fun delete(contactId: Int) {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if(!contactRepo.existsById(contactId)) throw NotFoundException("contact not found")
        contactRepo.deleteById(contactId)
    }

    override fun changeEmail(contactId: Int, emailId: Int, email: String): ContactDTO {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (emailId <= 0) throw IllegalArgumentException("emailId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val e = emailRepository.findById(emailId).getOrNull() ?: throw NotFoundException("email not found")
        if(e.contact.size == 1)
            e.email = email
        else{
            contact.email.removeIf { it.getId() == emailId }
            e.contact.removeIf { it.getId() == contactId }
            val newE = emailRepository.save(Email(email, mutableListOf(contact)))
            contact.email.add(newE)
        }
        return contact.toDTO()
    }


    override fun addNewEmail(contactId: Int, email: String): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        if(email.isBlank()) throw IllegalArgumentException("email must be not blank")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val emails = emailRepository.findByEmail(email)
        if(emails.isEmpty())
            emails.add(emailRepository.save(Email(email, mutableListOf())))
        contact.email.add(emails[0])
        return contact.toDTO()
    }

    override fun deleteEmail(contactId: Int, emailId: Int) {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (emailId <= 0) throw IllegalArgumentException("emailId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val email = emailRepository.findById(emailId).getOrNull() ?: throw NotFoundException("email not found")
        contact.email.remove(email)
        email.contact.removeIf { it.getId() == contactId }
        if(email.contact.isEmpty())
            emailRepository.deleteById(emailId)
    }

    override fun changeCategory(contactId: Int, category: Category): ContactDTO {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        contact.category = category
        return contact.toDTO()
    }

    override fun addAddress(contactId: Int, address: String): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        if(address.isBlank()) throw IllegalArgumentException("address must be not blank")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val add = addressRepository.findByAddress(address)
        val toAdd = if(add.isEmpty()) {
            val a = Address(address, mutableListOf(contact))
            addressRepository.save(a)
            a
        }
        else
            add[0]
        contact.address.add(toAdd)
        return contact.toDTO()
    }

    override fun changeAddress(contactId: Int, addressId: Int, address: String): ContactDTO {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (addressId <= 0) throw IllegalArgumentException("addressId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val add = addressRepository.findById(addressId).getOrNull() ?: throw NotFoundException("address not found")
        if (add.contact.size == 1)
            add.address = address
        else{
            contact.address.removeIf { it.getId() == addressId }
            add.contact.removeIf { it.getId() == contactId }
            val newAdd = addressRepository.save(Address(address, mutableListOf(contact)))
            contact.address.add(newAdd)
        }
        return contact.toDTO()
    }

    override fun deleteAddress(contactId: Int, addressId: Int) {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (addressId <= 0) throw IllegalArgumentException("addressId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val add = addressRepository.findById(addressId).getOrNull() ?: throw NotFoundException("address not found")
        contact.address.removeIf { it.getId() == addressId }
        add.contact.removeIf { it.getId() == contactId }
        if(add.contact.isEmpty())
            addressRepository.deleteById(addressId)
    }

    override fun addTelephone(contactId: Int, telephoneDTO: TelephoneDTO): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val tels = telephoneRepository.findByPrefixAndNumber(telephoneDTO.prefix, telephoneDTO.number)
        val tel = if(tels.isEmpty())
            telephoneRepository.save(Telephone(telephoneDTO.prefix, telephoneDTO.number, mutableListOf(contact)))
        else
            tels[0]
        contact.telephone.add(tel)
        return contact.toDTO()
    }

    override fun changeTelephone(contactId: Int, phoneId: Int, telephoneDTO: TelephoneDTO): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        if (phoneId < 0) throw IllegalArgumentException("phoneId must be >= 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val tel = telephoneRepository.findById(phoneId).getOrNull() ?: throw NotFoundException("telephone not found")
        if(tel.contact.size == 1){
            tel.prefix = telephoneDTO.prefix
            tel.number = telephoneDTO.number
        }
        else{
            contact.telephone.removeIf { it.getId() == phoneId }
            tel.contact.removeIf { it.getId() == contactId }
            val newTel = telephoneRepository.save(Telephone(telephoneDTO.prefix, telephoneDTO.number, mutableListOf(contact)))
            contact.telephone.add(newTel)
        }
        return contact.toDTO()
    }

    override fun deleteTelephone(contactId: Int, phoneId: Int): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        if (phoneId < 0) throw IllegalArgumentException("phoneId must be >= 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val tel = telephoneRepository.findById(phoneId).getOrNull() ?: throw NotFoundException("telephone not found")
        contact.telephone.removeIf { it.getId() == phoneId }
        tel.contact.removeIf { it.getId() == contactId }
        if(tel.contact.isEmpty())
            telephoneRepository.removeById(phoneId)
        return contact.toDTO()
    }
}