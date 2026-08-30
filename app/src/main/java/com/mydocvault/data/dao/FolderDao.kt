package com.mydocvault.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mydocvault.data.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE parentFolderId IS :parentId ORDER BY name")
    fun getFoldersByParent(parentId: Long?): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentFolderId IS :parentId ORDER BY name")
    suspend fun getFoldersByParentOnce(parentId: Long?): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY name")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): FolderEntity?

    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun getFolderByIdFlow(folderId: Long): Flow<FolderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFolders(query: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentFolderId IS :parentId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoldersByParent(parentId: Long?, query: String): Flow<List<FolderEntity>>

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteById(folderId: Long)
}
