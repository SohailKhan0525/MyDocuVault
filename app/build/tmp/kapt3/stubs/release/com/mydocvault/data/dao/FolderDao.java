package com.mydocvault.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.mydocvault.data.entity.FolderEntity;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ#\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\f2\b\u0010\u0010\u001a\u0004\u0018\u00010\tH\'\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\r2\b\u0010\u0010\u001a\u0004\u0018\u00010\tH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0016"}, d2 = {"Lcom/mydocvault/data/dao/FolderDao;", "", "delete", "", "folder", "Lcom/mydocvault/data/entity/FolderEntity;", "(Lcom/mydocvault/data/entity/FolderEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "folderId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllFolders", "Lkotlinx/coroutines/flow/Flow;", "", "getFolderById", "getFoldersByParent", "parentId", "(Ljava/lang/Long;)Lkotlinx/coroutines/flow/Flow;", "getFoldersByParentOnce", "(Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "update", "app_release"})
@androidx.room.Dao()
public abstract interface FolderDao {
    
    @androidx.room.Query(value = "SELECT * FROM folders WHERE parentFolderId IS :parentId ORDER BY name")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> getFoldersByParent(@org.jetbrains.annotations.Nullable()
    java.lang.Long parentId);
    
    @androidx.room.Query(value = "SELECT * FROM folders WHERE parentFolderId IS :parentId ORDER BY name")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFoldersByParentOnce(@org.jetbrains.annotations.Nullable()
    java.lang.Long parentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.mydocvault.data.entity.FolderEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM folders ORDER BY name")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> getAllFolders();
    
    @androidx.room.Query(value = "SELECT * FROM folders WHERE id = :folderId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFolderById(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.FolderEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.FolderEntity folder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.FolderEntity folder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.FolderEntity folder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM folders WHERE id = :folderId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}