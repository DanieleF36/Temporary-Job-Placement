package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.dtos.toDTO
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

    override fun create(contactDTO: ContactDTO): ContactDTO {
        val emailEntity = mutableListOf<Email>()
        contactDTO.email.forEach { emailStr ->
            val emails = emailRepository.findByEmail(emailStr)
            val email = if(emails.size == 0)
                emailRepository.save(Email(emailStr, emptyList()))
            else
                emails[0]
            emailEntity.add(email)
        }

        val addressEntity = mutableListOf<Address>()
        contactDTO.address.forEach { addrStr ->
            val adds = addressRepository.findByAddress(addrStr)
            val add = if(adds.size == 0)
                addressRepository.save(Address(addrStr, emptyList()))
            else
                adds[0]
            addressEntity.add(add)
        }

        val telephoneEntity = mutableListOf<Telephone>()
        contactDTO.telephone.forEach { telStr ->
            val prefix = telStr.subSequence(0, 2).toString().toInt()
            val number = telStr.subSequence(2, telStr.length).toString().toInt()
            val tels = telephoneRepository.findByPrefixAndNumber(prefix, number)
            val tel = if(tels.size == 0)
                telephoneRepository.save(Telephone(prefix, number, emptyList()))
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

    override fun addNewEmail(contactId: Int, email: String): ContactDTO {
        if (contactId < 0) throw IllegalArgumentException("id must be >= 0")
        if(email.isBlank()) throw IllegalArgumentException("email must be not blank")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val emails = emailRepository.findByEmail(email)
        if(emails.isEmpty())
            emailRepository.save(Email(email, emptyList()))
        contact.email.add(emails[0])
        return contact.toDTO()
    }

    override fun deleteEmail(contactId: Int, emailId: Int) {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (emailId <= 0) throw IllegalArgumentException("emailId must be > 0")
        if(!contactRepo.existsById(contactId)) throw NotFoundException("contact not found")
        if(!emailRepository.existsById(emailId)) throw NotFoundException("email not found")
        emailRepository.deleteById(emailId)
    }

    override fun changeCategory(contactId: Int, category: Category): ContactDTO {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        contact.category = category
        return contact.toDTO()
    }

    override fun changeAddress(contactId: Int, addressId: Int): ContactDTO {
        if (contactId <= 0) throw IllegalArgumentException("contactId must be > 0")
        if (addressId <= 0) throw IllegalArgumentException("addressId must be > 0")
        val contact = contactRepo.findById(contactId).getOrNull() ?: throw NotFoundException("contact not found")
        val address = addressRepository.findById(addressId).getOrNull() ?: throw NotFoundException("address not found")
        if (contact.address.find { it.address ==  address.address} == null)
            contact.address.add(address)
        return contact.toDTO()
    }
}