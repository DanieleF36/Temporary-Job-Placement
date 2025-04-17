package it.daniele.temporaryjobplacement.dtos

import it.daniele.temporaryjobplacement.annotation.OptionalNotBlank

data class UpdateContactDTO(
    @field:OptionalNotBlank val name: String?,
    @field:OptionalNotBlank val surname: String?,
    @field:OptionalNotBlank val ssn: String?,
)
