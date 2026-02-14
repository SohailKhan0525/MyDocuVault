package com.mydocvault.data.repository;

import android.net.Uri;
import com.mydocvault.data.dao.DocumentDao;
import com.mydocvault.data.dao.FolderDao;
import com.mydocvault.data.entity.DocumentEntity;
import com.mydocvault.data.entity.FolderEntity;
import com.mydocvault.utils.VaultFileManager;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nH\u0016J \u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u000eH\u0096@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u0016J#\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u000b0\n2\b\u0010\u0018\u001a\u0004\u0018\u00010\u000eH\u0016\u00a2\u0006\u0002\u0010\u001bJ#\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\n2\b\u0010\u0011\u001a\u0004\u0018\u00010\u000eH\u0016\u00a2\u0006\u0002\u0010\u001bJ\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u0015\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u0016J\u0018\u0010\u001e\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0018\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u0016J0\u0010\u001f\u001a\u00020\u000e2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u00102\b\u0010\u0018\u001a\u0004\u0018\u00010\u000e2\u0006\u0010#\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010$J \u0010%\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000e2\b\u0010&\u001a\u0004\u0018\u00010\u000eH\u0096@\u00a2\u0006\u0002\u0010\'J\u001e\u0010(\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000e2\u0006\u0010)\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010*J\u001e\u0010+\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010)\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010*J\u001e\u0010,\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000e2\u0006\u0010 \u001a\u00020!H\u0096@\u00a2\u0006\u0002\u0010-R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/mydocvault/data/repository/VaultRepositoryImpl;", "Lcom/mydocvault/data/repository/VaultRepository;", "folderDao", "Lcom/mydocvault/data/dao/FolderDao;", "documentDao", "Lcom/mydocvault/data/dao/DocumentDao;", "fileManager", "Lcom/mydocvault/utils/VaultFileManager;", "(Lcom/mydocvault/data/dao/FolderDao;Lcom/mydocvault/data/dao/DocumentDao;Lcom/mydocvault/utils/VaultFileManager;)V", "allFolders", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/mydocvault/data/entity/FolderEntity;", "createFolder", "", "name", "", "parentId", "(Ljava/lang/String;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteDocument", "", "documentId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFolderRecursive", "folderId", "documentsByFolder", "Lcom/mydocvault/data/entity/DocumentEntity;", "(Ljava/lang/Long;)Lkotlinx/coroutines/flow/Flow;", "foldersByParent", "getDocument", "getFolder", "importDocument", "uri", "Landroid/net/Uri;", "displayName", "fileType", "(Landroid/net/Uri;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "moveDocument", "newFolderId", "(JLjava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "renameDocument", "newName", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "renameFolder", "replaceDocument", "(JLandroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class VaultRepositoryImpl implements com.mydocvault.data.repository.VaultRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.data.dao.FolderDao folderDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.data.dao.DocumentDao documentDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.utils.VaultFileManager fileManager = null;
    
    @javax.inject.Inject()
    public VaultRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.dao.FolderDao folderDao, @org.jetbrains.annotations.NotNull()
    com.mydocvault.data.dao.DocumentDao documentDao, @org.jetbrains.annotations.NotNull()
    com.mydocvault.utils.VaultFileManager fileManager) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> foldersByParent(@org.jetbrains.annotations.Nullable()
    java.lang.Long parentId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.FolderEntity>> allFolders() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.mydocvault.data.entity.DocumentEntity>> documentsByFolder(@org.jetbrains.annotations.Nullable()
    java.lang.Long folderId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getFolder(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.FolderEntity> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getDocument(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.mydocvault.data.entity.DocumentEntity> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object createFolder(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.Long parentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object renameFolder(long folderId, @org.jetbrains.annotations.NotNull()
    java.lang.String newName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteFolderRecursive(long folderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object importDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.Long folderId, @org.jetbrains.annotations.NotNull()
    java.lang.String fileType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object renameDocument(long documentId, @org.jetbrains.annotations.NotNull()
    java.lang.String newName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteDocument(long documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object replaceDocument(long documentId, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object moveDocument(long documentId, @org.jetbrains.annotations.Nullable()
    java.lang.Long newFolderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}