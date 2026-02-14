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
public final class DocumentViewModel_Factory implements Factory<DocumentViewModel> {
  private final Provider<VaultRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public DocumentViewModel_Factory(Provider<VaultRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public DocumentViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static DocumentViewModel_Factory create(Provider<VaultRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new DocumentViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static DocumentViewModel newInstance(VaultRepository repository,
      SavedStateHandle savedStateHandle) {
    return new DocumentViewModel(repository, savedStateHandle);
  }
}
