package com.mydocvault.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.mydocvault.data.entity.DocumentEntity;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ#\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u000e0\r2\b\u0010\u000f\u001a\u0004\u0018\u00010\tH\'\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00050\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\tH\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0015"}, d2 = {"Lcom/mydocvault/data/dao/DocumentDao;", "", "delete", "", "document", "Lcom/mydocvault/data/entity/DocumentEntity;", "(Lcom/mydocvault/data/entity/DocumentEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "documentId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDocumentById", "getDocumentsByFolder", "Lkotlinx/coroutines/flow/Flow;", "", "folderId", "(Ljava/lang/Long;)Lkotlinx/coroutines/flow/Flow;", "getDocumentsByFolderOnce", "(Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "update", "app_debug"})
@androidx.room.Dao()
public abstract interface DocumentDao {
    
    @androidx.room.Query(value = "SELECT * FROM documents WHERE folderId IS :folderId ORDER BY updatedAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.DocumentEntity>> getDocumentsByFolder(@org.jetbrains.annotations.Nullable()
    java.lang.Long folderId);
    
    @androidx.room.Query(value = "SELECT * FROM documents WHERE id = :documentId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDocumentById(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.DocumentEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.DocumentEntity document, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.DocumentEntity document, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.entity.DocumentEntity document, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM documents WHERE id = :documentId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM documents WHERE folderId IS :folderId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDocumentsByFolderOnce(@org.jetbrains.annotations.Nullable()
    java.lang.Long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.mydocvault.data.entity.DocumentEntity>> $completion);
}