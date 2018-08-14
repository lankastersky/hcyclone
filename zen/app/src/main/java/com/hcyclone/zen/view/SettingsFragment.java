package com.hcyclone.zen.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.service.PreferencesService;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    getActivity().setTitle(getString(R.string.fragment_settings));
    return view;
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.preferences);

    // Bind the summaries of EditText/List/Dialog/Ringtone preferences
    // to their values. When their values change, their summaries are
    // updated to reflect the new value, per the Android Design
    // guidelines.
    PreferencesService preferencesService = PreferencesService.getInstance();
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_DAILY_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_FINAL_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_CHALLENGES_LANGUAGE_LIST));

    // Showing all challenges in the Journal by default. Set visible for beta testing if needed.
    findPreference(PreferencesService.PREF_KEY_SHOW_CHALLENGES).setVisible(Utils.isDebug());
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

  @Override
  public void onStart() {
    super.onStart();
    PreferenceManager.getDefaultSharedPreferences(getActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    PreferenceManager.getDefaultSharedPreferences(getActivity())
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    AppLifecycleManager.getInstance().requestBackup();
  }
}
