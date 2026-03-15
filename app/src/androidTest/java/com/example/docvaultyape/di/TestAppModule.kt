package com.example.docvaultyape.di

import com.example.docvaultyape.fake.FakeDocumentRepository
import com.example.docvaultyape.domain.repository.DocumentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideFakeDocumentRepository(): DocumentRepository = FakeDocumentRepository()
}