package com.mydocvault.utils;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class UpdateChecker_Factory implements Factory<UpdateChecker> {
  private final Provider<OkHttpClient> clientProvider;

  public UpdateChecker_Factory(Provider<OkHttpClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public UpdateChecker get() {
    return newInstance(clientProvider.get());
  }

  public static UpdateChecker_Factory create(Provider<OkHttpClient> clientProvider) {
    return new UpdateChecker_Factory(clientProvider);
  }

  public static UpdateChecker newInstance(OkHttpClient client) {
    return new UpdateChecker(client);
  }
}
