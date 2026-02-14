package com.mydocvault.viewmodel;

import com.mydocvault.data.preferences.UserPreferences;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<UserPreferences> prefsProvider;

  public AuthViewModel_Factory(Provider<UserPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<UserPreferences> prefsProvider) {
    return new AuthViewModel_Factory(prefsProvider);
  }

  public static AuthViewModel newInstance(UserPreferences prefs) {
    return new AuthViewModel(prefs);
  }
}
