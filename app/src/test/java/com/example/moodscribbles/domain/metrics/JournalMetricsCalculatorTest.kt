package com.example.moodscribbles.domain.metrics

import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class JournalMetricsCalculatorTest {

    private val calculator = JournalMetricsCalculator()
    private val fixedInstant = Instant.parse("2026-01-15T10:00:00Z")

    @Test
    fun compute_empty_returnsNullAveragesAndZeroBuckets() {
        val m = calculator.compute(emptyList())
        assertEquals(0, m.entryCount)
        assertNull(m.averageMoodScore)
        assertNull(m.averageEnergy)
        assertTrue(m.moodTrend.isEmpty())
        assertTrue(m.emotionFrequencies.isEmpty())
        assertEquals(3, m.energyBuckets.size)
        assertEquals(0, m.energyBuckets.sumOf { it.count })
    }

    @Test
    fun compute_threeEntries_averagesMoodEnergy_emotionShares_energyBuckets_sortedTrend() {
        val e1 = JournalEntry.newDraft(
            date = LocalDate.of(2026, 1, 3),
            mood = Mood.HAPPY,
            energyLevel = 80,
            title = null,
            description = null,
            emotion = Emotion(1L, "happy"),
            tags = emptyList(),
            now = fixedInstant,
        ).copy(id = 1L)
        val e2 = JournalEntry.newDraft(
            date = LocalDate.of(2026, 1, 1),
            mood = Mood.NEUTRAL,
            energyLevel = 40,
            title = null,
            description = null,
            emotion = Emotion(2L, "happy"),
            tags = emptyList(),
            now = fixedInstant,
        ).copy(id = 2L)
        val e3 = JournalEntry.newDraft(
            date = LocalDate.of(2026, 1, 2),
            mood = Mood.SAD,
            energyLevel = 10,
            title = null,
            description = null,
            emotion = Emotion(3L, "sad"),
            tags = emptyList(),
            now = fixedInstant,
        ).copy(id = 3L)

        val m = calculator.compute(listOf(e1, e2, e3))

        assertEquals(3, m.entryCount)
        assertEquals(3f, m.averageMoodScore!!, 0.01f)
        assertEquals((80 + 40 + 10) / 3f, m.averageEnergy!!, 0.01f)

        assertEquals(
            listOf(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 1, 3),
            ),
            m.moodTrend.map { it.date },
        )
        assertEquals(listOf(3f, 2f, 4f), m.moodTrend.map { it.moodScore })

        val happy = m.emotionFrequencies.find { it.emotionName == "happy" }!!
        val sad = m.emotionFrequencies.find { it.emotionName == "sad" }!!
        assertEquals(2, happy.count)
        assertEquals(1, sad.count)
        assertEquals(2f / 3f, happy.share, 0.001f)
        assertEquals(1f / 3f, sad.share, 0.001f)

        val low = m.energyBuckets.find { it.band == EnergyBand.LOW }!!.count
        val mid = m.energyBuckets.find { it.band == EnergyBand.MID }!!.count
        val high = m.energyBuckets.find { it.band == EnergyBand.HIGH }!!.count
        assertEquals(1, low)
        assertEquals(1, mid)
        assertEquals(1, high)
    }
}
