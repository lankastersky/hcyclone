package com.hcyclone.zen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.hcyclone.zen.AppLifecycleManager;

public final class PreferencesService {

  public static final String PREF_KEY_SHOW_NOTIFICATION = "pref_show_notification";
  public static final String PREF_KEY_INITIAL_ALARM_LIST = "pref_initial_reminder_list";
  public static final String PREF_KEY_DAILY_ALARM_LIST = "pref_constant_reminder_list";
  public static final String PREF_KEY_FINAL_ALARM_LIST = "pref_final_reminder_list";
  public static final String PREF_KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
  public static final String PREF_KEY_NOTIFICATION_VIBRATE = "notification_vibrate";
  public static final String PREF_KEY_SHOW_CHALLENGES = "pref_show_challenges";
  private static final PreferencesService instance = new PreferencesService();
  /**
   * A preference value change listener that updates the preference's summary
   * to reflect its new value.
   */
  private final Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener
      = new Preference.OnPreferenceChangeListener() {

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
      String stringValue = value.toString();

      if (preference instanceof ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(
            index >= 0
                ? listPreference.getEntries()[index]
                : null);
      } else {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        preference.setSummary(stringValue);
      }
      AppLifecycleManager.getInstance().requestBackup();
      return true;
    }
  };
  private SharedPreferences sharedPreferences;

  private PreferencesService() {
  }

  public static PreferencesService getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  /**
   * Binds a preference's summary to its value. More specifically, when the
   * preference's value is changed, its summary (line of text below the
   * preference title) is updated to reflect the value. The summary is also
   * immediately updated upon calling this method. The exact display format is
   * dependent on the type of preference.
   *
   * @see #bindPreferenceSummaryToValueListener
   */
  public void bindPreferenceSummaryToValue(@NonNull Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
        sharedPreferences.getString(preference.getKey(), ""));
  }
}
