package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.*

@Entity
data class DocumentBinaryData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    val content: ByteArray,
    @OneToOne(mappedBy = "binaryContent", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    val metadata: DocumentMetadata
)