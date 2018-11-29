package com.hcyclone.zen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.hcyclone.zen.Utils;

// Determines which feature are enabled for which users. Track the app version persistently.
public final class FeaturesService {

  // Which features sets are available.
  public enum FeaturesType {
    // Only free features are available.
    FREE,
    // Paid and free features are available.
    PAID
  }

  private static final String KEY_VERSION_CODE = "version_code";
  private static final String KEY_UPGRADED_USER = "upgraded_user";
  private static final String KEY_EXTENDED_VERSION = "extended_version";

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
   *   return {@link FeaturesType#PAID}
   * a new user paid for the content:
   *   return {@link FeaturesType#PAID}
   * else return {@link FeaturesType#FREE}
   */
  public FeaturesType getFeaturesType() {
    if (isUpgradedUser()) {
        // Upgraded users have all features for free.
        return FeaturesType.PAID;
    }

    if (getExtendedVersion()) {
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

  public void storeExtendedVersion(boolean enable) {
    sharedPreferences.edit().putBoolean(KEY_EXTENDED_VERSION, enable).apply();
  }

  private boolean getExtendedVersion() {
    return sharedPreferences.getBoolean(KEY_EXTENDED_VERSION, false);
  }

  private boolean isUpgradedUser() {
    return sharedPreferences.getBoolean(KEY_UPGRADED_USER, false);
  }

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
