package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
data class DocumentMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    var name: String,
    var size: Int,
    @Column(name = "content_type")
    var contentType: String,
    @Column(name = "creation_timestamp")
    val creationTimestamp: ZonedDateTime,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @MapsId
    @JoinColumn(name = "id")
    var binaryContent: DocumentBinaryData? = null,
)