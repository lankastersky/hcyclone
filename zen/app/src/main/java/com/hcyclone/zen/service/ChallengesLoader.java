package com.hcyclone.zen.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.ChallengeModel;

import java.util.Calendar;
import java.util.Date;

/** Loads challenges from Firebase if offline or from persistent storage. */
public final class ChallengesLoader {

  private static final String TAG = ChallengesLoader.class.getSimpleName();
  private static final String KEY_CHALLENGES_LAST_LOAD_TIME = "challenges_last_load_time";
  private static final Calendar CALENDAR = Calendar.getInstance();
  private static final int LOAD_AFTER_DAYS = 7;

  private final ChallengesLoadListener listener;
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
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

  /** Returns true if {@link #LOAD_AFTER_DAYS} passed since last load. */
  private boolean isTimeToLoad() {
    if (Utils.isDebug()) {
      return true;
    }
    long lastLoadTimeSec = sharedPreferences.getLong(KEY_CHALLENGES_LAST_LOAD_TIME, 0);
    if (lastLoadTimeSec == 0) {
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
    context.startService(intent);
  }

  private void onLoaded() {
    Log.d(TAG, "Challenges loaded");
    AlarmService.getInstance().setAlarms();
    ChallengeModel.getInstance().loadChallenges();
    if (listener != null) {
      listener.onChallengesLoaded();
    }
  }
}
