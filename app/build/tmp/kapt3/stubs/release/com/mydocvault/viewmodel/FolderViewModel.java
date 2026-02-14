package com.mydocvault.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.mydocvault.data.entity.DocumentEntity;
import com.mydocvault.data.entity.FolderEntity;
import com.mydocvault.data.repository.VaultRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015J\u000e\u0010\u0016\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u000eJ\u000e\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u0019\u001a\u00020\u000eJ\u001e\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u001d\u001a\u00020\u0015J\u001d\u0010\u001e\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u000e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0015J\u0016\u0010\"\u001a\u00020\u00132\u0006\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0015J\u0016\u0010#\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u001cR\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\f\u00a8\u0006$"}, d2 = {"Lcom/mydocvault/viewmodel/FolderViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/mydocvault/data/repository/VaultRepository;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "(Lcom/mydocvault/data/repository/VaultRepository;Landroidx/lifecycle/SavedStateHandle;)V", "documents", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/mydocvault/data/entity/DocumentEntity;", "getDocuments", "()Lkotlinx/coroutines/flow/StateFlow;", "folderId", "", "subfolders", "Lcom/mydocvault/data/entity/FolderEntity;", "getSubfolders", "createFolder", "", "name", "", "deleteDocument", "documentId", "deleteFolder", "targetId", "importDocument", "uri", "Landroid/net/Uri;", "type", "moveDocument", "newFolderId", "(JLjava/lang/Long;)V", "renameDocument", "renameFolder", "replaceDocument", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class FolderViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.data.repository.VaultRepository repository = null;
    private final long folderId = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> subfolders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.DocumentEntity>> documents = null;
    
    @javax.inject.Inject()
    public FolderViewModel(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.repository.VaultRepository repository, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.SavedStateHandle savedStateHandle) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> getSubfolders() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.DocumentEntity>> getDocuments() {
        return null;
    }
    
    public final void createFolder(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void renameFolder(long targetId, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void deleteFolder(long targetId) {
    }
    
    public final void renameDocument(long documentId, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void deleteDocument(long documentId) {
    }
    
    public final void replaceDocument(long documentId, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public final void moveDocument(long documentId, @org.jetbrains.annotations.Nullable()
    java.lang.Long newFolderId) {
    }
    
    public final void importDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
}