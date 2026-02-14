package com.mydocvault.di;

import android.content.Context;
import com.mydocvault.utils.VaultFileManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideFileManagerFactory implements Factory<VaultFileManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideFileManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VaultFileManager get() {
    return provideFileManager(contextProvider.get());
  }

  public static AppModule_ProvideFileManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideFileManagerFactory(contextProvider);
  }

  public static VaultFileManager provideFileManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFileManager(context));
  }
}
