package it.daniele.temporaryjobplacement.services.contact

import it.daniele.temporaryjobplacement.dtos.ContactDTO
import it.daniele.temporaryjobplacement.dtos.toDTO
import it.daniele.temporaryjobplacement.repositories.ContactRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Transactional
class ContactServiceImpl(private val contactRepo: ContactRepository): ContactService {
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

}