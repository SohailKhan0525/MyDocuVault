package com.mydocvault.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.mydocvault.data.dao.DocumentDao;
import com.mydocvault.data.dao.FolderDao;
import com.mydocvault.data.entity.DocumentEntity;
import com.mydocvault.data.entity.FolderEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lcom/mydocvault/data/db/VaultDatabase;", "Landroidx/room/RoomDatabase;", "()V", "documentDao", "Lcom/mydocvault/data/dao/DocumentDao;", "folderDao", "Lcom/mydocvault/data/dao/FolderDao;", "app_debug"})
@androidx.room.Database(entities = {com.mydocvault.data.entity.FolderEntity.class, com.mydocvault.data.entity.DocumentEntity.class}, version = 1, exportSchema = false)
public abstract class VaultDatabase extends androidx.room.RoomDatabase {
    
    public VaultDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.mydocvault.data.dao.FolderDao folderDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.mydocvault.data.dao.DocumentDao documentDao();
}