package com.example.moodscribbles.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.moodscribbles.data.local.AppDatabase
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import com.example.moodscribbles.data.preferences.appSettingsDataStore
import com.example.moodscribbles.data.repository.JournalRepositoryImpl
import com.example.moodscribbles.notifications.MoodReminderPreferenceRepository
import com.example.moodscribbles.domain.metrics.JournalMetricsCalculator
import com.example.moodscribbles.domain.repository.JournalRepository
import com.example.moodscribbles.domain.usecase.CreateJournalEntryUseCase
import com.example.moodscribbles.domain.usecase.GetJournalEntryByDateUseCase
import com.example.moodscribbles.domain.usecase.ObserveDashboardMetricsUseCase
import com.example.moodscribbles.domain.usecase.UpdateJournalEntryUseCase
import com.example.moodscribbles.ui.calendar.CalendarDayDetailViewModel
import com.example.moodscribbles.ui.calendar.CalendarViewModel
import com.example.moodscribbles.ui.history.JournalHistoryViewModel
import com.example.moodscribbles.ui.journal.JournalViewModel
import com.example.moodscribbles.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.LocalDate

/**
 * Root Koin module. Add single/factory/viewModel definitions as features grow.
 */
val appModule = module {
    single<DataStore<Preferences>> { androidContext().appSettingsDataStore }

    single { ThemePreferenceRepository(dataStore = get()) }

    single { MoodReminderPreferenceRepository(dataStore = get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.NAME,
        ).build()
    }

    single { get<AppDatabase>().journalDao() }
    single { get<AppDatabase>().emotionDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().entryTagCrossRefDao() }

    single<JournalRepository> {
        JournalRepositoryImpl(
            appDatabase = get(),
            journalDao = get(),
            emotionDao = get(),
            tagDao = get(),
            entryTagCrossRefDao = get(),
        )
    }

    factory { CreateJournalEntryUseCase(journalRepository = get()) }
    factory { UpdateJournalEntryUseCase(journalRepository = get()) }
    factory { GetJournalEntryByDateUseCase(journalRepository = get()) }

    factory { JournalMetricsCalculator() }
    factory {
        ObserveDashboardMetricsUseCase(
            journalRepository = get(),
            calculator = get(),
        )
    }

    viewModel {
        JournalViewModel(
            getJournalEntryByDate = get(),
            createJournalEntry = get(),
            updateJournalEntry = get(),
        )
    }

    viewModel {
        CalendarViewModel(journalRepository = get())
    }

    viewModel {
        JournalHistoryViewModel(
            journalRepository = get(),
            calculator = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            themePreferenceRepository = get(),
            moodReminderPreferenceRepository = get(),
            appContext = androidContext(),
        )
    }

    viewModel { (date: LocalDate) ->
        CalendarDayDetailViewModel(journalRepository = get(), date = date)
    }
}
