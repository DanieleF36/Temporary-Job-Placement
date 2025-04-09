package it.daniele.temporaryjobplacement.controllers

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun validateSort(allowedSort: List<String>, sort: String?, default: String): Sort {
    return if (!sort.isNullOrBlank()) {
        val sortParams = sort.split(",")
        val sortField = sortParams[0]

        if (!allowedSort.contains(sortField)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort option not allowed: $sortField")
        }

        return if (sortParams.size > 1) {
            val direction = try {
                Sort.Direction.fromString(sortParams[1])
            } catch (ex: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordering option not allowed: ${sortParams[1]}")
            }
            Sort.by(direction, sortField)
        } else {
            Sort.by(sortField)
        }
    } else {
        Sort.by("default").descending()
    }
}