package com.mydocvault.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.mydocvault.data.repository.VaultRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class FolderViewModel_Factory implements Factory<FolderViewModel> {
  private final Provider<VaultRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public FolderViewModel_Factory(Provider<VaultRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public FolderViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static FolderViewModel_Factory create(Provider<VaultRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new FolderViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static FolderViewModel newInstance(VaultRepository repository,
      SavedStateHandle savedStateHandle) {
    return new FolderViewModel(repository, savedStateHandle);
  }
}
