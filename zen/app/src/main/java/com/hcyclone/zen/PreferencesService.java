package com.hcyclone.zen;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public final class PreferencesService {

  private static final PreferencesService instance = new PreferencesService();

  public static final String PREF_KEY_INITIAL_ALARM_LIST = "pref_initial_reminder_list";
  public static final String PREF_KEY_REMINDER_ALARM_LIST = "pref_constant_reminder_list";
  public static final String PREF_KEY_FINAL_ALARM_LIST = "pref_final_reminder_list";
  public static final String PREF_KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
  public static final String PREF_KEY_NOTIFICATION_VIBRATE = "notification_vibrate";

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
   * A preference value change listener that updates the preference's summary
   * to reflect its new value.
   */
  private Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener
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
      } else if (preference instanceof RingtonePreference) {
        // For ringtone preferences, look up the correct display value
        // using RingtoneManager.
        if (TextUtils.isEmpty(stringValue)) {
          // Empty values correspond to 'silent' (no ringtone).
          preference.setSummary(R.string.pref_ringtone_silent);
        } else {
          Ringtone ringtone = RingtoneManager.getRingtone(
              preference.getContext(), Uri.parse(stringValue));
          if (ringtone == null) {
            // Clear the summary if there was a lookup error.
            preference.setSummary(null);
          } else {
            // Set the summary to reflect the new ringtone display
            // name.
            String name = ringtone.getTitle(preference.getContext());
            preference.setSummary(name);
          }
        }
      } else {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        preference.setSummary(stringValue);
      }
      return true;
    }
  };

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
