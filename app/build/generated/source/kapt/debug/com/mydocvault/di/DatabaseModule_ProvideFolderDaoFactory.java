package com.mydocvault.di;

import com.mydocvault.data.dao.FolderDao;
import com.mydocvault.data.db.VaultDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideFolderDaoFactory implements Factory<FolderDao> {
  private final Provider<VaultDatabase> dbProvider;

  public DatabaseModule_ProvideFolderDaoFactory(Provider<VaultDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public FolderDao get() {
    return provideFolderDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideFolderDaoFactory create(Provider<VaultDatabase> dbProvider) {
    return new DatabaseModule_ProvideFolderDaoFactory(dbProvider);
  }

  public static FolderDao provideFolderDao(VaultDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFolderDao(db));
  }
}
