package com.hcyclone.zen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.hcyclone.zen.Utils;

// Determines which feature are enabled for which users. Track the app version persistently.
public final class FeaturesService {

  public enum FeaturesType {
    FREE,
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

  public FeaturesType getFeaturesType() {
//    if (Utils.isDebug()) {
//      return FeaturesType.PAID;
//    }

    if (getVersionCode() <= FREE_FEATURES_CODE_VERSION) {
        // First users have all features for free.
        return FeaturesType.PAID;
      }
      return FeaturesType.FREE;
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
