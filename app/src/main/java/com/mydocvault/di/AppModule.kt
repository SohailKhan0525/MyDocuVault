package com.mydocvault.di

import android.content.Context
import com.mydocvault.utils.VaultFileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideFileManager(
        @ApplicationContext context: Context,
        userPreferences: com.mydocvault.data.preferences.UserPreferences
    ): VaultFileManager {
        return VaultFileManager(context, userPreferences)
    }
}
