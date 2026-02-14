package com.mydocvault.data.repository;

import com.mydocvault.data.dao.DocumentDao;
import com.mydocvault.data.dao.FolderDao;
import com.mydocvault.utils.VaultFileManager;
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
public final class VaultRepositoryImpl_Factory implements Factory<VaultRepositoryImpl> {
  private final Provider<FolderDao> folderDaoProvider;

  private final Provider<DocumentDao> documentDaoProvider;

  private final Provider<VaultFileManager> fileManagerProvider;

  public VaultRepositoryImpl_Factory(Provider<FolderDao> folderDaoProvider,
      Provider<DocumentDao> documentDaoProvider, Provider<VaultFileManager> fileManagerProvider) {
    this.folderDaoProvider = folderDaoProvider;
    this.documentDaoProvider = documentDaoProvider;
    this.fileManagerProvider = fileManagerProvider;
  }

  @Override
  public VaultRepositoryImpl get() {
    return newInstance(folderDaoProvider.get(), documentDaoProvider.get(), fileManagerProvider.get());
  }

  public static VaultRepositoryImpl_Factory create(Provider<FolderDao> folderDaoProvider,
      Provider<DocumentDao> documentDaoProvider, Provider<VaultFileManager> fileManagerProvider) {
    return new VaultRepositoryImpl_Factory(folderDaoProvider, documentDaoProvider, fileManagerProvider);
  }

  public static VaultRepositoryImpl newInstance(FolderDao folderDao, DocumentDao documentDao,
      VaultFileManager fileManager) {
    return new VaultRepositoryImpl(folderDao, documentDao, fileManager);
  }
}
