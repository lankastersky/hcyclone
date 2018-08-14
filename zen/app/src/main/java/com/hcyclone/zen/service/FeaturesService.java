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

  private static final FeaturesService instance = new FeaturesService();
  private static final String KEY_VERSION_CODE = "version_code";
  private static final int FREE_FEATURES_CODE_VERSION = 18;

  private Context context;
  private SharedPreferences sharedPreferences;

  public static FeaturesService getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    if (sharedPreferences == null) {
      this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    storeVersionCode();
  }

  /**
   * Cases:
   * an old user updates (version code less {@code FREE_FEATURES_CODE_VERSION}):
   *   return {@code FeaturesType.PAID}
   * a new user installs ({@link Utils.isFirstInstall(Context) returns true}):
   *   return {@code FeaturesType.FREE}
   * a new user paid for the content
   *   return {@code FeaturesType.PAID}
   * else return {@code FeaturesType.FREE}
   */
  public FeaturesType getFeaturesType() {
//    if (Utils.isDebug()) {
//      return FeaturesType.PAID;
//    }

    if (getVersionCode() <= FREE_FEATURES_CODE_VERSION) {
        // First users have all features for free.
        return FeaturesType.PAID;
    }

    if (Utils.isFirstInstall(context)) {
      return FeaturesType.FREE;
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

  private void storeVersionCode() {
    // Store version code only once.
    if (getVersionCode() == 0) {
      int versionCode = Utils.getVersionCode(context);
      sharedPreferences.edit().putInt(KEY_VERSION_CODE, versionCode).apply();
    }
  }

  private int getVersionCode() {
    return sharedPreferences.getInt(KEY_VERSION_CODE, 0);
  }
}
