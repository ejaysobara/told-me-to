package com.example.someonetoldmeto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import com.example.someonetoldmeto.model.PersonScore
import com.example.someonetoldmeto.model.RecStatus
import com.example.someonetoldmeto.model.RecType
import com.example.someonetoldmeto.model.Recommendation
import com.example.someonetoldmeto.model.Rules

class RecRepository(private val db: SQLiteDatabase) {

    fun save(id: Long?, title: String, person: String, type: RecType, note: String?, link: String?): Long = db.transaction {
        val values = ContentValues().apply {
            put("person_id", personIdFor(person))
            put("title", title)
            put("type", type.db)
            put("note", note)
            put("link", link)
        }
        if (id == null) {
            values.put("created_at", System.currentTimeMillis())
            insertOrThrow("recommendation", null, values)
        } else {
            update("recommendation", values, "rec_id = ?", arrayOf(id.toString()))
            id
        }
    }

    fun list(query: String = "", type: RecType? = null, status: RecStatus? = null, personId: Long? = null): List<Recommendation> {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (query.isNotBlank()) {
            where += "(r.title LIKE ? ESCAPE '\\' OR r.note LIKE ? ESCAPE '\\' OR p.name LIKE ? ESCAPE '\\')"
            repeat(3) { args += "%" + Rules.escapeLike(query.trim()) + "%" }
        }
        type?.let { where += "r.type = ?"; args += it.db }
        status?.let { where += "r.status = ?"; args += it.db }
        personId?.let { where += "r.person_id = ?"; args += it.toString() }
        val clause = if (where.isEmpty()) "" else " WHERE " + where.joinToString(" AND ")
        return recs("$SELECT$clause ORDER BY r.created_at DESC, r.rec_id DESC", args.toTypedArray())
    }

    fun untriedIds(type: RecType?): List<Long> = db.rawQuery(
        "SELECT rec_id FROM recommendation WHERE status = 'untried'" + if (type != null) " AND type = ?" else "",
        type?.let { arrayOf(it.db) }
    ).use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }

    fun peopleScores(): List<PersonScore> = Rules.rankPeople(scores(null))

    fun get(id: Long): Recommendation? = recs("$SELECT WHERE r.rec_id = ?", arrayOf(id.toString())).firstOrNull()

    fun rate(id: Long, status: RecStatus) {
        db.update("recommendation", ContentValues().apply { put("status", status.db) }, "rec_id = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) = db.delete("recommendation", "rec_id = ?", arrayOf(id.toString())) > 0

    fun score(personId: Long): PersonScore? = scores(personId).firstOrNull()

    private fun scores(personId: Long?): List<PersonScore> = db.rawQuery(
        """SELECT p.person_id, p.name, COUNT(r.rec_id), SUM(r.status = 'liked'), SUM(r.status <> 'untried')
           FROM person p JOIN recommendation r ON r.person_id = p.person_id""" +
            (if (personId != null) " WHERE p.person_id = ?" else "") + " GROUP BY p.person_id",
        personId?.let { arrayOf(it.toString()) }
    ).use { c -> buildList { while (c.moveToNext()) add(PersonScore(c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4))) } }

    fun personNames(prefix: String): List<String> = db.rawQuery(
        "SELECT name FROM person WHERE name LIKE ? ESCAPE '\\' ORDER BY name LIMIT 5",
        arrayOf(Rules.escapeLike(prefix) + "%")
    ).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }

    private fun personIdFor(name: String): Long {
        db.rawQuery("SELECT person_id FROM person WHERE name = ?", arrayOf(name)).use {
            if (it.moveToFirst()) return it.getLong(0)
        }
        return db.insertOrThrow("person", null, ContentValues().apply {
            put("name", name)
            put("created_at", System.currentTimeMillis())
        })
    }

    private fun recs(sql: String, args: Array<String>?) = db.rawQuery(sql, args).use { c ->
        buildList {
            while (c.moveToNext()) add(
                Recommendation(
                    id = c.getLong(0), personId = c.getLong(1), person = c.getString(2), title = c.getString(3),
                    type = RecType.of(c.getString(4)), note = c.getString(5), link = c.getString(6),
                    status = RecStatus.of(c.getString(7)), createdAt = c.getLong(8),
                )
            )
        }
    }

    companion object {
        private const val SELECT = """SELECT r.rec_id, r.person_id, p.name, r.title, r.type, r.note, r.link, r.status, r.created_at
            FROM recommendation r JOIN person p ON p.person_id = r.person_id"""

        private var shared: RecRepository? = null

        fun get(context: Context) =
            shared ?: RecRepository(AppDbHelper(context.applicationContext).writableDatabase).also { shared = it }
    }
}
