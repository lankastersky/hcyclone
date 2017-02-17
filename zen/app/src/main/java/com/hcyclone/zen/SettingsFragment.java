package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragmentCompat {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    getActivity().setTitle(getString(R.string.fragment_settings));
    return view;
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.pref_notification);

    // Bind the summaries of EditText/List/Dialog/Ringtone preferences
    // to their values. When their values change, their summaries are
    // updated to reflect the new value, per the Android Design
    // guidelines.
    PreferencesService preferencesService = PreferencesService.getInstance();
//    preferencesService.bindPreferenceSummaryToValue(findPreference(
//        PreferencesService.PREF_KEY_NOTIFICATION_RINGTONE));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_DAILY_ALARM_LIST));
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
