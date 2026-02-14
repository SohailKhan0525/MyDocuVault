package com.mydocvault.di;

import android.content.Context;
import androidx.room.Room;
import com.mydocvault.data.dao.DocumentDao;
import com.mydocvault.data.dao.FolderDao;
import com.mydocvault.data.db.VaultDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\t\u001a\u00020\u0004H\u0007\u00a8\u0006\f"}, d2 = {"Lcom/mydocvault/di/DatabaseModule;", "", "()V", "provideDatabase", "Lcom/mydocvault/data/db/VaultDatabase;", "context", "Landroid/content/Context;", "provideDocumentDao", "Lcom/mydocvault/data/dao/DocumentDao;", "db", "provideFolderDao", "Lcom/mydocvault/data/dao/FolderDao;", "app_release"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DatabaseModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.mydocvault.di.DatabaseModule INSTANCE = null;
    
    private DatabaseModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.mydocvault.data.db.VaultDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.mydocvault.data.dao.FolderDao provideFolderDao(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.db.VaultDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.mydocvault.data.dao.DocumentDao provideDocumentDao(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.db.VaultDatabase db) {
        return null;
    }
}