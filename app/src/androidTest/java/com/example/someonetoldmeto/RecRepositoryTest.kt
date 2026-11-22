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

    @Test fun searchMatchesTitleNoteAndNameIgnoringCase() {
        repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        repo.save(null, "Blue", "Mika", RecType.SONG, "sevenfold remix", null)
        repo.save(null, "Dune", "Seven", RecType.BOOK, null, null)
        repo.save(null, "Ramen", "Jon", RecType.PLACE, null, null)
        assertEquals(setOf("Severance", "Blue", "Dune"), repo.list("SEV").map { it.title }.toSet())
    }

    @Test fun searchTreatsWildcardsAsText() {
        repo.save(null, "50% off ramen", "Jon", RecType.PLACE, null, null)
        repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        assertEquals(listOf("50% off ramen"), repo.list("%").map { it.title })
    }

    @Test fun filtersCombine() {
        val sev = repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        repo.save(null, "Seven Nation Army", "Ana", RecType.SONG, null, null)
        repo.save(null, "Seventeen", "Jon", RecType.SONG, null, null)
        val ana = repo.get(sev)!!.personId
        assertEquals(listOf("Seven Nation Army"), repo.list("sev", RecType.SONG, null, ana).map { it.title })
        repo.rate(sev, RecStatus.LIKED)
        assertEquals(listOf("Severance"), repo.list(status = RecStatus.LIKED).map { it.title })
    }

    @Test fun untriedIdsRespectType() {
        val a = repo.save(null, "Severance", "Ana", RecType.SHOW, null, null)
        val b = repo.save(null, "Dark", "Ana", RecType.SHOW, null, null)
        repo.save(null, "Blue", "Mika", RecType.SONG, null, null)
        repo.rate(b, RecStatus.LIKED)
        assertEquals(listOf(a), repo.untriedIds(RecType.SHOW))
        assertEquals(2, repo.untriedIds(null).size)
    }

    @Test fun peopleRankedBestFirstAndHiddenWithoutPicks() {
        val a1 = repo.save(null, "A1", "Ana", RecType.OTHER, null, null)
        val a2 = repo.save(null, "A2", "Ana", RecType.OTHER, null, null)
        val j1 = repo.save(null, "J1", "Jon", RecType.OTHER, null, null)
        val m1 = repo.save(null, "M1", "Mika", RecType.OTHER, null, null)
        repo.save(null, "M2", "Mika", RecType.OTHER, null, null)
        repo.rate(a1, RecStatus.LIKED)
        repo.rate(a2, RecStatus.DISLIKED)
        repo.rate(j1, RecStatus.LIKED)
        repo.delete(m1)
        val gone = repo.save(null, "Z", "Zed", RecType.OTHER, null, null)
        repo.delete(gone)
        assertEquals(listOf("Jon", "Ana", "Mika"), repo.peopleScores().map { it.name })
    }

    @Test fun suggestionsMatchPrefixAndEscapeWildcards() {
        repo.save(null, "A", "Ana", RecType.OTHER, null, null)
        repo.save(null, "B", "Andrei", RecType.OTHER, null, null)
        repo.save(null, "C", "Mika", RecType.OTHER, null, null)
        assertEquals(listOf("Ana", "Andrei"), repo.personNames("an"))
        assertEquals(emptyList<String>(), repo.personNames("%"))
    }
}
