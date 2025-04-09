package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.*

@Entity
class DocumentBinaryData(
    var content: ByteArray,
    @OneToOne(mappedBy = "binaryContent", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    val metadata: DocumentMetadata
): EntityBase()