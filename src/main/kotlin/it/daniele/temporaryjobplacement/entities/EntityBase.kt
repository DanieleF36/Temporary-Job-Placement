package it.daniele.temporaryjobplacement.entities

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.util.ProxyUtils

    @MappedSuperclass
    abstract class EntityBase {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private var id: Int = 0

        fun getId(): Int = id

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other === this) return true
            if (javaClass != ProxyUtils.getUserClass(other)) return false
            other as EntityBase
            return this.id == other.id
        }

        override fun hashCode(): Int {
            return 17
        }
    }