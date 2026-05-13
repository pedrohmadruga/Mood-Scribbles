package com.example.moodscribbles.domain.metrics

import com.example.moodscribbles.domain.JournalEntry
import java.time.LocalDate

class JournalMetricsCalculator {

    fun compute(
        entries: List<JournalEntry>,
        periodStart: LocalDate? = null,
        periodEnd: LocalDate? = null,
    ): DashboardMetrics {
        if (entries.isEmpty()) {
            return DashboardMetrics.empty(periodStart = periodStart, periodEnd = periodEnd)
        }

        val sorted = entries.sortedBy { it.date }
        val scores = sorted.map { it.mood.toScore() }
        val averageMood = scores.average().toFloat()
        val averageEnergy = sorted.map { it.energyLevel.toDouble() }.average().toFloat()

        val moodTrend = sorted.map { MoodTrendPoint(date = it.date, moodScore = it.mood.toScore()) }

        val labeled = sorted.filter { it.emotion.name.trim().isNotEmpty() }
        val totalLabeled = labeled.size
        val emotionFrequencies = if (totalLabeled == 0) {
            emptyList()
        } else {
            labeled
                .groupBy { it.emotion.name.trim() }
                .map { (name, list) ->
                    val count = list.size
                    EmotionFrequency(
                        emotionName = name,
                        count = count,
                        share = count.toFloat() / totalLabeled.toFloat(),
                    )
                }
                .sortedWith(compareByDescending<EmotionFrequency> { it.count }.thenBy { it.emotionName })
        }

        var low = 0
        var mid = 0
        var high = 0
        for (e in sorted) {
            when {
                e.energyLevel <= 33 -> low++
                e.energyLevel <= 66 -> mid++
                else -> high++
            }
        }
        val energyBuckets = listOf(
            EnergyBucket(EnergyBand.LOW, low),
            EnergyBucket(EnergyBand.MID, mid),
            EnergyBucket(EnergyBand.HIGH, high),
        )

        return DashboardMetrics(
            periodStart = periodStart,
            periodEnd = periodEnd,
            entryCount = entries.size,
            averageMoodScore = averageMood,
            averageEnergy = averageEnergy,
            moodTrend = moodTrend,
            emotionFrequencies = emotionFrequencies,
            energyBuckets = energyBuckets,
        )
    }
}
