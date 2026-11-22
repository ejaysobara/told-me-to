package com.example.someonetoldmeto.model

import kotlin.random.Random

enum class Problem { EMPTY, TOO_LONG }

object Rules {
    const val TITLE_MAX = 100
    const val PERSON_MAX = 40
    const val TEXT_MAX = 500

    fun check(text: String, max: Int, required: Boolean = true): Problem? = when {
        required && text.isBlank() -> Problem.EMPTY
        text.trim().length > max -> Problem.TOO_LONG
        else -> null
    }

    fun normalizeName(raw: String) = raw.trim().replace(Regex("\\s+"), " ")

    fun escapeLike(text: String) = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    fun pick(ids: List<Long>, current: Long? = null, random: Random = Random.Default): Long? =
        (if (ids.size > 1) ids.filter { it != current } else ids).randomOrNull(random)

    fun rankPeople(people: List<PersonScore>) = people.sortedWith(
        compareByDescending<PersonScore> { it.tried > 0 }
            .thenByDescending { if (it.tried == 0) 0.0 else it.liked.toDouble() / it.tried }
            .thenByDescending { it.tried }
            .thenBy { it.name.lowercase() }
    )
}
