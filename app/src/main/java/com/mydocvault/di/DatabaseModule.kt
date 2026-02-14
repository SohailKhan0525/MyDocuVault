package com.mydocvault.di

import android.content.Context
import androidx.room.Room
import com.mydocvault.data.dao.DocumentDao
import com.mydocvault.data.dao.FolderDao
import com.mydocvault.data.db.VaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VaultDatabase {
        return Room.databaseBuilder(context, VaultDatabase::class.java, "vault.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFolderDao(db: VaultDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideDocumentDao(db: VaultDatabase): DocumentDao = db.documentDao()
}
