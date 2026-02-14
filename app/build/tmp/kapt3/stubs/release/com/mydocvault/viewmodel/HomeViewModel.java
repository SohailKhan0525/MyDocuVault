package com.mydocvault.viewmodel;

import androidx.lifecycle.ViewModel;
import com.mydocvault.data.entity.FolderEntity;
import com.mydocvault.data.repository.VaultRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011J\u001e\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0015\u001a\u00020\u000eJ\u0016\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\r\u001a\u00020\u000eR\u001d\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/mydocvault/viewmodel/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/mydocvault/data/repository/VaultRepository;", "(Lcom/mydocvault/data/repository/VaultRepository;)V", "folders", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/mydocvault/data/entity/FolderEntity;", "getFolders", "()Lkotlinx/coroutines/flow/StateFlow;", "createFolder", "", "name", "", "deleteFolder", "folderId", "", "importDocument", "uri", "Landroid/net/Uri;", "type", "renameFolder", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.mydocvault.data.repository.VaultRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> folders = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.mydocvault.data.repository.VaultRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.mydocvault.data.entity.FolderEntity>> getFolders() {
        return null;
    }
    
    public final void createFolder(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void renameFolder(long folderId, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void deleteFolder(long folderId) {
    }
    
    public final void importDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
}