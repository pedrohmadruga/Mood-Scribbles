package com.example.moodscribbles.core.di

import androidx.room.Room
import com.example.moodscribbles.data.local.AppDatabase
import com.example.moodscribbles.data.repository.JournalRepositoryImpl
import com.example.moodscribbles.domain.repository.JournalRepository
import com.example.moodscribbles.domain.usecase.CreateJournalEntryUseCase
import com.example.moodscribbles.domain.usecase.GetJournalEntryByDateUseCase
import com.example.moodscribbles.domain.usecase.UpdateJournalEntryUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Root Koin module. Add single/factory/viewModel definitions as features grow.
 */
val appModule = module {
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
}
