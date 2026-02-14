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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001d\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0016\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001aR\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/mydocvault/viewmodel/DocumentViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/mydocvault/data/repository/VaultRepository;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "(Lcom/mydocvault/data/repository/VaultRepository;Landroidx/lifecycle/SavedStateHandle;)V", "_document", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/mydocvault/data/entity/DocumentEntity;", "document", "Lkotlinx/coroutines/flow/StateFlow;", "getDocument", "()Lkotlinx/coroutines/flow/StateFlow;", "documentId", "", "folders", "", "Lcom/mydocvault/data/entity/FolderEntity;", "getFolders", "moveDocument", "", "folderId", "(JLjava/lang/Long;)V", "replaceDocument", "uri", "Landroid/net/Uri;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class DocumentViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.data.repository.VaultRepository repository = null;
    private final long documentId = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.mydocvault.data.entity.DocumentEntity> _document = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.mydocvault.data.entity.DocumentEntity> document = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> folders = null;
    
    @javax.inject.Inject()
    public DocumentViewModel(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.repository.VaultRepository repository, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.SavedStateHandle savedStateHandle) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.mydocvault.data.entity.DocumentEntity> getDocument() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> getFolders() {
        return null;
    }
    
    public final void moveDocument(long documentId, @org.jetbrains.annotations.Nullable()
    java.lang.Long folderId) {
    }
    
    public final void replaceDocument(long documentId, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
}