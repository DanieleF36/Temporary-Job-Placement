package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
data class DocumentMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    val name: String,
    val size: Int,
    @Column(name = "content_type")
    val contentType: String,
    @Column(name = "creation_timestamp")
    val creationTimestamp: ZonedDateTime,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @MapsId
    @JoinColumn(name = "id")
    var binaryContent: DocumentBinaryData? = null,
)