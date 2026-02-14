package com.mydocvault.data.repository;

import android.net.Uri;
import com.mydocvault.data.entity.DocumentEntity;
import com.mydocvault.data.entity.FolderEntity;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\r\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H&J \u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ#\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00040\u00032\b\u0010\u0011\u001a\u0004\u0018\u00010\u0007H&\u00a2\u0006\u0002\u0010\u0014J#\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\u0010\n\u001a\u0004\u0018\u00010\u0007H&\u00a2\u0006\u0002\u0010\u0014J\u0018\u0010\u0016\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u000e\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u0018\u0010\u0017\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0011\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u000fJ0\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\t2\b\u0010\u0011\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u001c\u001a\u00020\tH\u00a6@\u00a2\u0006\u0002\u0010\u001dJ \u0010\u001e\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00072\b\u0010\u001f\u001a\u0004\u0018\u00010\u0007H\u00a6@\u00a2\u0006\u0002\u0010 J\u001e\u0010!\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\tH\u00a6@\u00a2\u0006\u0002\u0010#J\u001e\u0010$\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\tH\u00a6@\u00a2\u0006\u0002\u0010#J\u001e\u0010%\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00072\u0006\u0010\u0019\u001a\u00020\u001aH\u00a6@\u00a2\u0006\u0002\u0010&\u00a8\u0006\'"}, d2 = {"Lcom/mydocvault/data/repository/VaultRepository;", "", "allFolders", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/mydocvault/data/entity/FolderEntity;", "createFolder", "", "name", "", "parentId", "(Ljava/lang/String;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteDocument", "", "documentId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFolderRecursive", "folderId", "documentsByFolder", "Lcom/mydocvault/data/entity/DocumentEntity;", "(Ljava/lang/Long;)Lkotlinx/coroutines/flow/Flow;", "foldersByParent", "getDocument", "getFolder", "importDocument", "uri", "Landroid/net/Uri;", "displayName", "fileType", "(Landroid/net/Uri;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "moveDocument", "newFolderId", "(JLjava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "renameDocument", "newName", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "renameFolder", "replaceDocument", "(JLandroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface VaultRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> foldersByParent(@org.jetbrains.annotations.Nullable()
    java.lang.Long parentId);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> allFolders();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.DocumentEntity>> documentsByFolder(@org.jetbrains.annotations.Nullable()
    java.lang.Long folderId);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFolder(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.FolderEntity> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDocument(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.DocumentEntity> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createFolder(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.Long parentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object renameFolder(long folderId, @org.jetbrains.annotations.NotNull()
    java.lang.String newName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFolderRecursive(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object importDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.Long folderId, @org.jetbrains.annotations.NotNull()
    java.lang.String fileType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object renameDocument(long documentId, @org.jetbrains.annotations.NotNull()
    java.lang.String newName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteDocument(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object replaceDocument(long documentId, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object moveDocument(long documentId, @org.jetbrains.annotations.Nullable()
    java.lang.Long newFolderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}