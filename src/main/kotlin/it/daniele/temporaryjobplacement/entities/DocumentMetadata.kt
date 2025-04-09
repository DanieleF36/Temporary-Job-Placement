package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
class DocumentMetadata(
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
): EntityBase()