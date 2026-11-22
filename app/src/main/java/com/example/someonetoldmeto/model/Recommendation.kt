package com.example.someonetoldmeto.model

enum class RecType(val db: String) {
    SHOW("Show"), SONG("Song"), PLACE("Place"), BOOK("Book"), OTHER("Other");

    companion object {
        fun of(db: String) = entries.first { it.db == db }
    }
}

enum class RecStatus(val db: String) {
    UNTRIED("untried"), LIKED("liked"), DISLIKED("disliked");

    companion object {
        fun of(db: String) = entries.first { it.db == db }
    }
}

data class Recommendation(
    val id: Long,
    val personId: Long,
    val person: String,
    val title: String,
    val type: RecType,
    val note: String?,
    val link: String?,
    val status: RecStatus,
    val createdAt: Long,
)

data class PersonScore(val id: Long, val name: String, val total: Int, val liked: Int, val tried: Int)
