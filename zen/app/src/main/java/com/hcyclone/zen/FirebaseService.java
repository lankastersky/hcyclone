package com.hcyclone.zen;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class FirebaseService extends IntentService
    implements FirebaseAdapter.FirebaseAuthListener {

  private static final String TAG = FirebaseService.class.getSimpleName();

  public static final String INTENT_KEY_RECEIVER = "INTENT_KEY_RECEIVER";
  public static final int RESULT_CODE_OK = 0;
  public static final int RESULT_CODE_ERROR = 1;

  private CountDownLatch countDownLatch;
  private ResultReceiver receiver;

  public FirebaseService() {
    super("FirebaseService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    receiver = intent.getParcelableExtra(INTENT_KEY_RECEIVER);
    countDownLatch = new CountDownLatch(1);
    if (!FirebaseAdapter.getInstance().isSignedIn()) {
      Log.d(TAG, "Sign in to Firebase");
      FirebaseAdapter.getInstance().signIn(this);
    } else {
      loadChallenges();
    }
    try {
      countDownLatch.await();
    } catch (InterruptedException exception) {
      Log.e(TAG, exception.toString());
    }
  }

  @Override
  public void onAuthSuccess() {
    loadChallenges();
  }

  @Override
  public void onAuthError(Exception exception) {
    Log.e(TAG, exception.toString());
    receiver.send(RESULT_CODE_ERROR, null);
    countDownLatch.countDown();
  }

  private void loadChallenges() {
    Log.d(TAG, "Load challenges");
    FirebaseAdapter.getInstance().getChallenges(new FirebaseAdapter.FirebaseDataListener() {
      @Override
      public void onError(Exception exception) {
        Log.e(TAG, exception.toString());
        if (receiver != null) {
          receiver.send(RESULT_CODE_ERROR, null);
        }
        countDownLatch.countDown();
      }

      @Override
      public void onChallenges(List<Challenge> challenges) {
        Log.d(TAG, "Challenges loaded");
        ChallengeModel.getInstance().loadChallenges(challenges);

        Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
        if (challenge.getStatus() == Challenge.ACCEPTED) {
          AlarmService.getInstance().setDailyAlarm();
        }

        if (receiver != null) {
          receiver.send(RESULT_CODE_OK, null);
        }
        countDownLatch.countDown();
      }
    });
  }
}
