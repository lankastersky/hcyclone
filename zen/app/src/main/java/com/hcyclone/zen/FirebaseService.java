package com.hcyclone.zen;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class FirebaseService extends Service implements FirebaseAdapter.FirebaseAuthListener {

  private final ServiceBinder binder = new ServiceBinder();
  private FirebaseAdapter.FirebaseDataListener firebaseDataListener;

  public class ServiceBinder extends Binder {
    public FirebaseService getService() {
      return FirebaseService.this;
    }
  }
    @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_REDELIVER_INTENT;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onAuthSuccess() {
    ChallengeModel.getInstance().loadChallenges(new FirebaseAdapter.FirebaseDataListener() {

      @Override
      public void onError(Exception exception) {
        if (firebaseDataListener != null) {
          firebaseDataListener.onError(exception);
        }
        stopSelf();
      }

      @Override
      public void onChallenges(List<Challenge> challenges) {
        if (firebaseDataListener != null) {
          firebaseDataListener.onChallenges(challenges);
        }
        stopSelf();
      }
    });
  }

  @Override
  public void onAuthError(Exception exception) {
    if (firebaseDataListener != null) {
      firebaseDataListener.onError(exception);
    }
    stopSelf();
  }

  public void loadChallenges(FirebaseAdapter.FirebaseDataListener listener) {
    this.firebaseDataListener = listener;
    FirebaseAdapter.getInstance().signIn(this);
  }
}
