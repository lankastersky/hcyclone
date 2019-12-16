package com.hcyclone.zen.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import com.crashlytics.android.Crashlytics;
import com.hcyclone.zen.App;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class FirebaseService extends IntentService
    implements FirebaseAdapter.FirebaseAuthListener {

  private static final String TAG = FirebaseService.class.getSimpleName();
  private static final int DOWNLOAD_CHALLENGES_WAIT_SEC = 30;

  public static final String INTENT_KEY_RECEIVER = "INTENT_KEY_RECEIVER";
  public static final int RESULT_CODE_OK = 0;
  public static final int RESULT_CODE_ERROR = 1;
  public static final String INTENT_CHALLENGES_LOCALE = "INTENT_CHALLENGES_LOCALE";

  private CountDownLatch countDownLatch;
  private ResultReceiver receiver;
  private String locale;

  public FirebaseService() {
    super("FirebaseService");
    setIntentRedelivery(true);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, startId, startId);
    return START_REDELIVER_INTENT;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    receiver = intent.getParcelableExtra(INTENT_KEY_RECEIVER);
    locale = intent.getStringExtra(INTENT_CHALLENGES_LOCALE);
    countDownLatch = new CountDownLatch(1);
    if (!FirebaseAdapter.getInstance().isSignedIn()) {
      Log.d(TAG, "Sign in to Firebase");
      FirebaseAdapter.getInstance().signIn(this, this);
    } else {
      downloadChallenges();
    }
    try {
      // Prevent service to be stopped before challenges were loaded.
      countDownLatch.await(DOWNLOAD_CHALLENGES_WAIT_SEC, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Log.e(TAG, e.toString());
      Crashlytics.logException(e);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void onAuthSuccess() {
    downloadChallenges();
  }

  @Override
  public void onAuthError(Exception e) {
    Log.e(TAG, e.toString());
    Crashlytics.logException(e);
    if (receiver != null) {
      receiver.send(RESULT_CODE_ERROR, null);
    }
    countDownLatch.countDown();
  }

  private void downloadChallenges() {
    Log.d(TAG, "Load challenges with locale " + locale);
    FirebaseAdapter.getInstance().downloadChallenges(locale, new ChallengesDownloadListener() {
      @Override
      public void onError(Exception e) {
        Log.e(TAG, "Failed to load challenges", e);
        Crashlytics.logException(e);
        if (receiver != null) {
          receiver.send(RESULT_CODE_ERROR, null);
        }
        countDownLatch.countDown();
      }

      @Override
      public void onChallengesDownloaded(List<Challenge> challenges) {
        Log.d(TAG, "Challenges loaded");
        ChallengeModel challengeModel = ((App) getApplicationContext()).getChallengeModel();
        challengeModel.saveChallenges(challenges);
        if (receiver != null) {
          receiver.send(RESULT_CODE_OK, null);
        }
        countDownLatch.countDown();
      }
    }, this);
  }
}
