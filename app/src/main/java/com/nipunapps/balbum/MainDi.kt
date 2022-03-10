package com.nipunapps.balbum

import android.content.Context
import com.nipunapps.balbum.storage.DirectoryRepository
import com.nipunapps.balbum.storage.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainDi {

    @Provides
    @Singleton
    fun provideStorageManager(
        @ApplicationContext context: Context
    ) : StorageRepository = StorageRepository(context)

    @Provides
    @Singleton
    fun provideDirectoryRepo(
        @ApplicationContext context: Context
    ) : DirectoryRepository = DirectoryRepository(context)
}