package com.example.docvaultyape.di

import android.content.Context
import androidx.room.Room
import com.example.docvaultyape.data.local.AppDatabase
import com.example.docvaultyape.data.local.dao.DocumentDao
import com.example.docvaultyape.data.repository.DocumentRepositoryImpl
import com.example.docvaultyape.domain.repository.DocumentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "docvault_db")
            .fallbackToDestructiveMigration().build()

    @Provides
    fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
}
