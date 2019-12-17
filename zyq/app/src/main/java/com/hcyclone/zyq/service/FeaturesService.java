package com.hcyclone.zyq.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import com.hcyclone.zyq.Utils;

// Determines which feature are enabled for which users. Track the app version persistently.
public final class FeaturesService {

  // Which features sets are available.
  public enum FeaturesType {
    // Only free features are available.
    FREE,
    // Paid and free features are available.
    PAID
  }

  private static final FeaturesService instance = new FeaturesService();
  private static final String KEY_VERSION_CODE = "version_code";
  private static final String KEY_UPGRADED_USER = "upgraded_user";

  private Context context;
  private SharedPreferences sharedPreferences;

  private FeaturesService() {}

  public static FeaturesService getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    if (sharedPreferences == null) {
      this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
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
    if (Utils.isDebug()) {
      return FeaturesType.FREE;
    }

    if (Utils.isFirstInstall(context)) {
      return FeaturesType.FREE;
    }

    if (isUpgradedUser()) {
        // Upgraded users have all features for free.
        return FeaturesType.PAID;
    }

    if (isPaidContent()) {
      return FeaturesType.PAID;
    }

    return FeaturesType.FREE;
  }

  private boolean isPaidContent() {
    // TODO: implement.
    return false;
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
