package com.hcyclone.zen;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService {

  private static final String TAG = NotificationService.class.getSimpleName();

  private static final NotificationService instance = new NotificationService();

  private static final int NOTIFICATION_ID = 1;

  private android.app.NotificationManager notificationManager;
  private Context context;

  private NotificationService() {
  }

  public static NotificationService getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;
    notificationManager = (android.app.NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public void showInitialAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (!(challenge.getStatus() == Challenge.UNKNOWN || challenge.getStatus() == Challenge.SHOWN)) {
      Log.d(TAG, "Ignore initial alarm notification as challenge not shown");
      return;
    }
    Log.d(TAG, "Show initial alarm notification");
    showNotification("New challenge available", challenge.getContent());
  }

  public void showFinalAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      // Show notification only for accepted challenge.
      Log.d(TAG, "Ignore final alarm notification as challenge not accepted");
      return;
    }
    Log.d(TAG, "Show final alarm notification");
    showNotification("Finish your challenge", challenge.getContent());
  }

  public void showReminderAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      Log.d(TAG, "Ignore reminder alarm notification as challenge not accepted");
      return;
    }
    Log.d(TAG, "Show reminder alarm notification");
    showNotification("Remember about your challenge", challenge.getContent());
  }

  private void showNotification(String title, String text) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true);
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
  }
}
