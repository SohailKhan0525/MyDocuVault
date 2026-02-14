package com.mydocvault.di;

import com.mydocvault.data.dao.DocumentDao;
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
public final class DatabaseModule_ProvideDocumentDaoFactory implements Factory<DocumentDao> {
  private final Provider<VaultDatabase> dbProvider;

  public DatabaseModule_ProvideDocumentDaoFactory(Provider<VaultDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DocumentDao get() {
    return provideDocumentDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDocumentDaoFactory create(
      Provider<VaultDatabase> dbProvider) {
    return new DatabaseModule_ProvideDocumentDaoFactory(dbProvider);
  }

  public static DocumentDao provideDocumentDao(VaultDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDocumentDao(db));
  }
}
