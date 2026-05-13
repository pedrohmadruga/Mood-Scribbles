package com.example.moodscribbles.domain.usecase

import com.example.moodscribbles.domain.metrics.DashboardMetrics
import com.example.moodscribbles.domain.metrics.JournalMetricsCalculator
import com.example.moodscribbles.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ObserveDashboardMetricsUseCase(
    private val journalRepository: JournalRepository,
    private val calculator: JournalMetricsCalculator,
) {

    operator fun invoke(startDateInclusive: LocalDate, endDateInclusive: LocalDate): Flow<DashboardMetrics> =
        journalRepository.observeEntriesByDateRange(startDateInclusive, endDateInclusive).map { entries ->
            calculator.compute(
                entries = entries,
                periodStart = startDateInclusive,
                periodEnd = endDateInclusive,
            )
        }
}
