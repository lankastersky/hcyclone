package com.hcyclone.zen.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.hcyclone.zen.App;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.ChallengeModel;

import java.util.Calendar;
import java.util.Date;

/** Loads challenges from Firebase if offline or from persistent storage. */
public final class ChallengesLoader implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = ChallengesLoader.class.getSimpleName();
  private static final String KEY_CHALLENGES_LAST_LOAD_TIME = "challenges_last_load_time";
  private static final int DEFAULT_LAST_LOAD_TIME = 0;
  private static final Calendar CALENDAR = Calendar.getInstance();
  private static final int LOAD_AFTER_DAYS = 7;

  private final ChallengesLoadListener listener;
  private final ChallengeModel challengeModel;
  private final SharedPreferences sharedPreferences;

  public interface ChallengesLoadListener {
    void onError(Exception e);
    void onChallengesLoaded();
  }

  private static class ChallengesResultReceiver extends ResultReceiver {

    final ChallengesLoader challengesLoader;

    ChallengesResultReceiver(Handler handler, ChallengesLoader challengesLoader) {
      super(handler);
      this.challengesLoader = challengesLoader;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);

      switch (resultCode) {
        case FirebaseService.RESULT_CODE_OK:
          challengesLoader.storeLastLoadTime();
          break;
        case FirebaseService.RESULT_CODE_ERROR:
          Log.w(TAG, "Failed to download challenges. Try to restore from persistent storage");
          break;
      }
      challengesLoader.onLoaded();
    }
  }

  public ChallengesLoader(ChallengesLoadListener listener, Context context) {
    this.listener = listener;
    challengeModel = ((App) context.getApplicationContext()).getChallengeModel();
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    PreferenceManager.getDefaultSharedPreferences(context)
        .registerOnSharedPreferenceChangeListener(this);

    if (getChallengesLocale() == null) {
      String language = Utils.getCurrentLocale(context).getLanguage();
      sharedPreferences
          .edit().putString(PreferencesService.PREF_KEY_CHALLENGES_LANGUAGE_LIST, language).apply();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(PreferencesService.PREF_KEY_CHALLENGES_LANGUAGE_LIST)) {
      // Reset load time to force the new update for the other language.
      sharedPreferences
          .edit().putLong(KEY_CHALLENGES_LAST_LOAD_TIME, DEFAULT_LAST_LOAD_TIME).apply();
    }
  }

  public void loadChallenges(Context context) {
    if (!isTimeToLoad()) {
      // Avoid frequent request to backend. Use data from persistent storage.
      onLoaded();
      return;
    }

    if (Utils.isOnline(context)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Avoid IllegalStateException: Not allowed to start service Intent.
        // See https://developer.android.com/about/versions/oreo/background.html.
        // When an app goes into the background, it has a window of several minutes in which it is
        // still allowed to create and use services.
        // TODO: use JobScheduler instead.
        if (AppLifecycleManager.isAppVisible()) {
          startFirebaseService(context);
        } else {
          Log.w(TAG, "Don't start service in background for Android O+");
          // Use data from persistent storage.
          onLoaded();
        }
      } else {
        startFirebaseService(context);
      }
    } else {
      Log.w(TAG, "Device is offline, Try to restore from persistent storage.");
      onLoaded();
    }
  }

  /** Returns true if {@link #LOAD_AFTER_DAYS} passed since last load or locale changed. */
  private boolean isTimeToLoad() {
//    if (Utils.isDebug()) {
//      return true;
//    }

    if (sharedPreferences.getBoolean(
        PreferencesService.PREF_KEY_SHOW_CHALLENGES, false)) {
      return true;
    }

    long lastLoadTimeSec =
        sharedPreferences.getLong(KEY_CHALLENGES_LAST_LOAD_TIME, DEFAULT_LAST_LOAD_TIME);
    if (lastLoadTimeSec == DEFAULT_LAST_LOAD_TIME) {
      return true;
    }

    Date lastLoadTime = new Date(lastLoadTimeSec);
    CALENDAR.setTime(lastLoadTime);
    CALENDAR.add(Calendar.DATE, LOAD_AFTER_DAYS);
    Date nextLoadTime = CALENDAR.getTime();
    Date now = new Date();
    return !nextLoadTime.after(now);
  }

  private void storeLastLoadTime() {
    long nowSec = new Date().getTime();
    sharedPreferences.edit().putLong(KEY_CHALLENGES_LAST_LOAD_TIME, nowSec).apply();
  }

  private void startFirebaseService(Context context) {
    Intent intent = new Intent(context, FirebaseService.class);
    intent.putExtra(FirebaseService.INTENT_KEY_RECEIVER,
        new ChallengesResultReceiver(new Handler(), this));
    intent.putExtra(FirebaseService.INTENT_CHALLENGES_LOCALE, getChallengesLocale());
    context.startService(intent);
  }


  private String getChallengesLocale() {
    return sharedPreferences.getString(
        PreferencesService.PREF_KEY_CHALLENGES_LANGUAGE_LIST, null);
  }

  private void onLoaded() {
    Log.d(TAG, "Challenges loaded");
    AlarmService.getInstance().setAlarms();
    challengeModel.loadChallenges();
    if (listener != null) {
      listener.onChallengesLoaded();
    }
  }
}
