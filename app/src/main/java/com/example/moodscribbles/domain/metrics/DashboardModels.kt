package com.example.moodscribbles.domain.metrics

import java.time.LocalDate

/** One point for a mood-over-time chart (date ascending in [DashboardMetrics.moodTrend]). */
data class MoodTrendPoint(
    val date: LocalDate,
    val moodScore: Float,
)

data class EmotionFrequency(
    val emotionName: String,
    val count: Int,
    val share: Float,
)

enum class EnergyBand {
    LOW,
    MID,
    HIGH,
}

/** Histogram bucket for energy distribution (0–100 scale). */
data class EnergyBucket(
    val band: EnergyBand,
    val count: Int,
)

data class DashboardMetrics(
    val periodStart: LocalDate?,
    val periodEnd: LocalDate?,
    val entryCount: Int,
    val averageMoodScore: Float?,
    val averageEnergy: Float?,
    val moodTrend: List<MoodTrendPoint>,
    val emotionFrequencies: List<EmotionFrequency>,
    val energyBuckets: List<EnergyBucket>,
)
