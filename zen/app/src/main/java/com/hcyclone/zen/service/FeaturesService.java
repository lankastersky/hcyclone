package com.hcyclone.zen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import com.hcyclone.zen.Utils;

// Determines which feature are enabled for which users. Track the app version persistently.
public final class FeaturesService {

  // Which features sets are available.
  public enum FeaturesType {
    // Only free features are available.
    FREE,
    // Paid and free features are available.
    PAID,
    // Features for upgraded users are available (same as paid mostly)
    UPGRADED
  }

  private static final String KEY_VERSION_CODE = "version_code";
  private static final String KEY_UPGRADED_USER = "upgraded_user";
  private static final String KEY_EXTENDED_VERSION_ACTIVATED = "extended_version_activated";
  private static final String KEY_EXTENDED_VERSION_DIALOG_SHOWN = "extended_version_dialog_shown";

  private final Context context;
  private final SharedPreferences sharedPreferences;

  public FeaturesService(@NonNull Context context) {
    this.context = context;
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    // Must be called before storing version code.
    storeUpgradedUserFlag();
    storeVersionCode();
  }

  /**
   * Cases:
   * a new user installs ({@link Utils#isFirstInstall(Context) returns true}):
   *   return {@link FeaturesType#FREE}
   * an upgraded user updates:
   *   return {@link FeaturesType#UPGRADED}
   * a user paid for the content:
   *   return {@link FeaturesType#PAID}
   * else return {@link FeaturesType#FREE}
   */
  public FeaturesType getFeaturesType() {
    if (isUpgradedUser()) {
        // Upgraded users have all features for free.
        return FeaturesType.UPGRADED;
    }

    if (isExtendedVersionActivated()) {
      return FeaturesType.PAID;
    }

    if (Utils.isFirstInstall(context)) {
      return FeaturesType.FREE;
    }

    if (Utils.isDebug()) {
      return FeaturesType.FREE;
    }

    return FeaturesType.FREE;
  }

  /**
   * Returns true if it's not a first install and the user is not upgraded and the dialog wasn't
   * shown before.
   */
  public boolean showExtendedVersionDialog() {
    return !Utils.isFirstInstall(context)
        && !isUpgradedUser()
        && !isExtendedVersionDialogShown();
  }

  /** Remembers if Extended version available dialog is shown. */
  public void storeShowExtendedVersionDialogShown(boolean shown) {
    sharedPreferences.edit().putBoolean(KEY_EXTENDED_VERSION_DIALOG_SHOWN, shown).apply();
  }

  private boolean isExtendedVersionDialogShown() {
    return sharedPreferences.getBoolean(KEY_EXTENDED_VERSION_DIALOG_SHOWN, false);
  }

  /** Remembers if the user is using the extended version. */
  public void storeExtendedVersionActivated(boolean enable) {
    sharedPreferences.edit().putBoolean(KEY_EXTENDED_VERSION_ACTIVATED, enable).apply();
  }

  private boolean isExtendedVersionActivated() {
    return sharedPreferences.getBoolean(KEY_EXTENDED_VERSION_ACTIVATED, false);
  }

  /** Returns true if the users was using our app before extended features (ads etc.). */
  private boolean isUpgradedUser() {
    if (Utils.isDebug()) {
      return sharedPreferences.getBoolean(
          PreferencesService.PREF_KEY_UPGRADED_USER, false);
    }
    return sharedPreferences.getBoolean(KEY_UPGRADED_USER, false);
  }

  /**
   * Upgraded users are users who used the version without extended features (ads etc.).
   * We didn't store versions of the app before that, so use it as a signal.
   */
  private void storeUpgradedUserFlag() {
    // Upgraded users don't have version code stored.
    if (!Utils.isFirstInstall(context) && getVersionCode() == 0) {
      sharedPreferences.edit().putBoolean(KEY_UPGRADED_USER, true).apply();
    }
  }

  private int getVersionCode() {
    return sharedPreferences.getInt(KEY_VERSION_CODE, 0);
  }

  private void storeVersionCode() {
    int versionCode = Utils.getVersionCode(context);
    sharedPreferences.edit().putInt(KEY_VERSION_CODE, versionCode).apply();
  }
}
