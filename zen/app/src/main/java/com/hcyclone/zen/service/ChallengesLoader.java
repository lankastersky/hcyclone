package com.hcyclone.zen.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.ChallengeModel;

/** Loads challenges from Firebase if offline or from persistent storage. */
public final class ChallengesLoader {

  private static final String TAG = ChallengesLoader.class.getSimpleName();

  public interface ChallengesLoadListener {
    void onError(Exception e);
    void onChallengesLoaded();
  }

  private static class ChallengesResultReceiver extends ResultReceiver {

    final ChallengesLoadListener listener;

    ChallengesResultReceiver(Handler handler, ChallengesLoadListener listener) {
      super(handler);
      this.listener = listener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);

      switch (resultCode) {
        case FirebaseService.RESULT_CODE_OK:
          // Do nothing.
          break;
        case FirebaseService.RESULT_CODE_ERROR:
          Log.w(TAG, "Failed to download challenges. Try to restore from persistent storage");
          break;
      }
      onLoaded(listener);
    }
  }

  public void loadChallenges(ChallengesLoadListener listener, Context context) {
    if (Utils.isOnline(context)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Avoid IllegalStateException: Not allowed to start service Intent.
        // See https://developer.android.com/about/versions/oreo/background.html.
        // When an app goes into the background, it has a window of several minutes in which it is
        // still allowed to create and use services.
        // TODO: use JobScheduler instead.
        if (AppLifecycleManager.isAppVisible()) {
          startFirebaseService(listener, context);
        } else {
          Log.w(TAG, "Don't start service in background for Android O+");
          // Try to restore from persistent storage.
          onLoaded(listener);
        }
      } else {
        startFirebaseService(listener, context);
      }
    } else {
      Log.w(TAG, "Device is offline, Try to restore from persistent storage.");
      onLoaded(listener);
    }
  }

  private static void startFirebaseService(ChallengesLoadListener listener, Context context) {
    Intent intent = new Intent(context, FirebaseService.class);
    intent.putExtra(FirebaseService.INTENT_KEY_RECEIVER,
        new ChallengesResultReceiver(new Handler(), listener));
    context.startService(intent);
  }

  private static void onLoaded(ChallengesLoadListener listener) {
    AlarmService.getInstance().setAlarms();
    ChallengeModel.getInstance().loadChallenges();
    if (listener != null) {
      listener.onChallengesLoaded();
    }
  }
}
