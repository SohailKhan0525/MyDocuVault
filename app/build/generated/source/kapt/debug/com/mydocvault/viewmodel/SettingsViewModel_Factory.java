package com.mydocvault.viewmodel;

import com.mydocvault.data.preferences.UserPreferences;
import com.mydocvault.utils.UpdateChecker;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferences> prefsProvider;

  private final Provider<UpdateChecker> updateCheckerProvider;

  public SettingsViewModel_Factory(Provider<UserPreferences> prefsProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    this.prefsProvider = prefsProvider;
    this.updateCheckerProvider = updateCheckerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(prefsProvider.get(), updateCheckerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferences> prefsProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    return new SettingsViewModel_Factory(prefsProvider, updateCheckerProvider);
  }

  public static SettingsViewModel newInstance(UserPreferences prefs, UpdateChecker updateChecker) {
    return new SettingsViewModel(prefs, updateChecker);
  }
}
