package com.hcyclone.zen.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.hcyclone.zen.App;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.service.BillingService;
import com.hcyclone.zen.service.PreferencesService;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

  public static final String TAG = SettingsFragment.class.getCanonicalName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    getActivity().setTheme(R.style.SettingsTheme);
    super.onCreate(savedInstanceState);
  }

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
    PreferencesService preferencesService = new PreferencesService(getContext());
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_DAILY_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_FINAL_ALARM_LIST));
    preferencesService.bindPreferenceSummaryToValue(findPreference(
        PreferencesService.PREF_KEY_CHALLENGES_LANGUAGE_LIST));

    Preference privacyPolicyButton = findPreference(PreferencesService.PREF_KEY_PRIVACY_POLICY);
    privacyPolicyButton.setOnPreferenceClickListener(preference -> {
      Intent intent = new Intent(getContext(), PrivacyPolicyActivity.class);
      startActivity(intent);
      return true;
    });

    // Showing all challenges in the Journal by default. Set visible for beta testing if needed.
    findPreference(PreferencesService.PREF_KEY_SHOW_CHALLENGES).setVisible(Utils.isDebug());
    // Activates upgraded user features.
    findPreference(PreferencesService.PREF_KEY_UPGRADED_USER).setVisible(Utils.isDebug());

    Preference clearPurchasesButton = findPreference(PreferencesService.PREF_KEY_CLEAR_PURCHASES);
    clearPurchasesButton.setVisible(Utils.isDebug());
    clearPurchasesButton.setOnPreferenceClickListener(preference -> {
      MainActivity mainActivity = (MainActivity) getActivity();
      BillingService billingService = mainActivity.getBillingService();
      billingService.clearPurchases(getContext());
      ((App) mainActivity.getApplication()).getFeaturesService().storeExtendedVersionActivated(false);
      return true;
    });
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
