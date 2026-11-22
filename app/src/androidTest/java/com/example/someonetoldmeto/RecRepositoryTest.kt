package com.example.someonetoldmeto

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.someonetoldmeto.data.AppDbHelper
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.model.RecStatus
import com.example.someonetoldmeto.model.RecType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecRepositoryTest {

    private lateinit var helper: AppDbHelper
    private lateinit var repo: RecRepository

    @Before fun setUp() {
        helper = AppDbHelper(InstrumentationRegistry.getInstrumentation().targetContext, null)
        repo = RecRepository(helper.writableDatabase)
    }

    @Test fun newestPickIsFirst() {
        repo.save(null, "Mang Inasal", "Kuya Jon", RecType.PLACE, null, null)
        Thread.sleep(2)
        repo.save(null, "Severance", "Ana", RecType.SHOW, "watch with subtitles", null)
        assertEquals(listOf("Severance", "Mang Inasal"), repo.list().map { it.title })
    }

    @Test fun personNameMatchesIgnoringCase() {
        val a = repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        val b = repo.save(null, "Dune", "ana", RecType.BOOK, null, null)
        assertEquals(repo.get(a)!!.personId, repo.get(b)!!.personId)
        assertEquals("Ana", repo.get(b)!!.person)
    }

    @Test fun editKeepsIdAndChangesFields() {
        val id = repo.save(null, "Severence", "Ana", RecType.OTHER, null, null)
        repo.save(id, "Severance", "Ana", RecType.SHOW, null, "https://example.com")
        val rec = repo.get(id)!!
        assertEquals("Severance", rec.title)
        assertEquals(RecType.SHOW, rec.type)
        assertEquals("https://example.com", rec.link)
        assertEquals(1, repo.list().size)
    }

    @Test fun ratingUpdatesTrustScore() {
        val ids = (1..3).map { repo.save(null, "Pick $it", "Ana", RecType.OTHER, null, null) }
        repo.rate(ids[0], RecStatus.LIKED)
        repo.rate(ids[1], RecStatus.LIKED)
        repo.rate(ids[2], RecStatus.DISLIKED)
        val score = repo.score(repo.get(ids[0])!!.personId)!!
        assertEquals(2, score.liked)
        assertEquals(3, score.tried)
    }

    @Test fun deleteRemovesPick() {
        val id = repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        assertTrue(repo.delete(id))
        assertNull(repo.get(id))
        assertFalse(repo.delete(id))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun personWithPicksCannotBeDeleted() {
        repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        helper.writableDatabase.execSQL("DELETE FROM person WHERE name = 'Ana'")
    }

    @Test fun suggestionsMatchPrefixAndEscapeWildcards() {
        repo.save(null, "A", "Ana", RecType.OTHER, null, null)
        repo.save(null, "B", "Andrei", RecType.OTHER, null, null)
        repo.save(null, "C", "Mika", RecType.OTHER, null, null)
        assertEquals(listOf("Ana", "Andrei"), repo.personNames("an"))
        assertEquals(emptyList<String>(), repo.personNames("%"))
    }
}
