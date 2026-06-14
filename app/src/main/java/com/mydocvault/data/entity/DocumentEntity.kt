package com.mydocvault.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val folderId: Long? = null,
    val filePath: String,
    val fileType: String,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
