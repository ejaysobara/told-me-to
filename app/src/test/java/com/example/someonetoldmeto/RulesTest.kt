package com.example.someonetoldmeto

import com.example.someonetoldmeto.model.PersonScore
import com.example.someonetoldmeto.model.Problem
import com.example.someonetoldmeto.model.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class RulesTest {

    @Test fun emptyTitleIsEmpty() = assertEquals(Problem.EMPTY, Rules.check("", Rules.TITLE_MAX))

    @Test fun spacesOnlyTitleIsEmpty() = assertEquals(Problem.EMPTY, Rules.check("   ", Rules.TITLE_MAX))

    @Test fun titleAtLimitIsFine() = assertNull(Rules.check("a".repeat(100), Rules.TITLE_MAX))

    @Test fun titleOverLimitIsTooLong() = assertEquals(Problem.TOO_LONG, Rules.check("a".repeat(101), Rules.TITLE_MAX))

    @Test fun limitCountsTrimmedText() = assertNull(Rules.check("  " + "a".repeat(100) + "  ", Rules.TITLE_MAX))

    @Test fun personOverLimitIsTooLong() = assertEquals(Problem.TOO_LONG, Rules.check("a".repeat(41), Rules.PERSON_MAX))

    @Test fun optionalNoteMayBeEmpty() = assertNull(Rules.check("", Rules.TEXT_MAX, required = false))

    @Test fun optionalLinkOverLimitIsTooLong() =
        assertEquals(Problem.TOO_LONG, Rules.check("a".repeat(501), Rules.TEXT_MAX, required = false))

    @Test fun nameIsTrimmedAndSpacesCollapsed() = assertEquals("Kuya Jon", Rules.normalizeName("  Kuya   Jon "))

    @Test fun likeWildcardsAreEscaped() = assertEquals("50\\% off\\_now\\\\", Rules.escapeLike("50% off_now\\"))

    @Test fun pickFromNothingIsNull() = assertNull(Rules.pick(emptyList()))

    @Test fun pickWithOneLeftReturnsIt() = assertEquals(7L, Rules.pick(listOf(7L), current = 7L))

    @Test fun pickAgainNeverRepeatsWhenOthersExist() {
        val random = Random(1)
        repeat(50) { assertEquals(true, Rules.pick(listOf(1L, 2L, 3L), current = 2L, random = random) != 2L) }
    }

    @Test fun peopleRankBestFirstAndUntriedLast() {
        val ana = PersonScore(1, "Ana", 5, 4, 5)
        val jon = PersonScore(2, "Kuya Jon", 4, 4, 4)
        val mika = PersonScore(3, "Mika", 2, 0, 0)
        val lee = PersonScore(4, "Lee", 3, 1, 3)
        assertEquals(listOf(jon, ana, lee, mika), Rules.rankPeople(listOf(mika, lee, ana, jon)))
    }

    @Test fun equalScoresRankByMoreTried() {
        val few = PersonScore(1, "Ana", 1, 1, 1)
        val many = PersonScore(2, "Ben", 3, 3, 3)
        assertEquals(listOf(many, few), Rules.rankPeople(listOf(few, many)))
    }
}
