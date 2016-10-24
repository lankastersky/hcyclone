package com.hcyclone.zen;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
  /**
   * Helper method to determine if the device has an extra-large screen. For
   * example, 10" tablets are extra-large.
   */
  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupActionBar();
    getFragmentManager().beginTransaction().replace(android.R.id.content,
        new NotificationPreferenceFragment()).commit();
  }

  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onIsMultiPane() {
    return isXLargeTablet(this);
  }

  /**
   * This method stops fragment injection in malicious applications.
   * Make sure to deny any unknown fragments here.
   */
  protected boolean isValidFragment(String fragmentName) {
    return PreferenceFragment.class.getName().equals(fragmentName)
        || NotificationPreferenceFragment.class.getName().equals(fragmentName);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class NotificationPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_notification);
      setHasOptionsMenu(true);

      // Bind the summaries of EditText/List/Dialog/Ringtone preferences
      // to their values. When their values change, their summaries are
      // updated to reflect the new value, per the Android Design
      // guidelines.
      PreferencesService preferencesService = PreferencesService.getInstance();
      preferencesService.bindPreferenceSummaryToValue(findPreference(
          PreferencesService.PREF_KEY_NOTIFICATION_RINGTONE));
      preferencesService.bindPreferenceSummaryToValue(findPreference(
          PreferencesService.PREF_KEY_INITIAL_ALARM_LIST));
      preferencesService.bindPreferenceSummaryToValue(findPreference(
          PreferencesService.PREF_KEY_REMINDER_ALARM_LIST));
      preferencesService.bindPreferenceSummaryToValue(findPreference(
          PreferencesService.PREF_KEY_FINAL_ALARM_LIST));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        getActivity().finish();
        return true;
      }
      return super.onOptionsItemSelected(item);
    }
  }
}
