package com.mydocvault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mydocvault.data.dao.DocumentDao
import com.mydocvault.data.dao.FolderDao
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity

@Database(
    entities = [FolderEntity::class, DocumentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun documentDao(): DocumentDao
}
