package com.hcyclone.zen.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/** Stores challenges filter data. */
public final class ChallengeFilterModel {

  private static final String PREF_KEY_CHALLENGES_FILTER_LEVEL = "challenges_filter_level";
  private static final String PREF_KEY_CHALLENGES_FILTER_RATING = "challenges_filter_rating";

  private SharedPreferences sharedPreferences;

  public ChallengeFilterModel(Context context) {
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public void restoreLevels(boolean[] items) {
    restoreArray(items, PREF_KEY_CHALLENGES_FILTER_LEVEL);
  }

  public void storeLevels(boolean[] items) {
    storeArray(items, PREF_KEY_CHALLENGES_FILTER_LEVEL);
  }

  public void restoreRatings(boolean[] items) {
    restoreArray(items, PREF_KEY_CHALLENGES_FILTER_RATING);
  }

  public void storeRatings(boolean[] items) {
    storeArray(items, PREF_KEY_CHALLENGES_FILTER_RATING);
  }

  private void storeArray(boolean[] items, String key) {
    Set<String> itemsSet = new HashSet<>();
    for (int i = 0; i < items.length; i++) {
      if (items[i]) {
        itemsSet.add(String.valueOf(i));
      }
    }
    sharedPreferences.edit().putStringSet(key, itemsSet).apply();
  }

  private void restoreArray(boolean[] items, String key) {
    Set<String> itemsSet = sharedPreferences.getStringSet(key, null);
    if (itemsSet != null) {
      for (int i = 0; i < items.length; i++) {
        items[i] = itemsSet.contains(String.valueOf(i));
      }
    }
  }
}
