package com.hcyclone.zen;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationManager {

  private static final String TAG = NotificationManager.class.getSimpleName();

  private static final NotificationManager instance = new NotificationManager();

  private static final int NOTIFICATION_ID_INITIAL = 1;
  private static final int NOTIFICATION_ID_FINAL = 2;
  private static final int NOTIFICATION_ID_REMINDER = 3;

  private android.app.NotificationManager notificationManager;
  private Context context;

  private NotificationManager() {
  }

  public static NotificationManager getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;
    notificationManager = (android.app.NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public void showInitialAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (!(challenge.getStatus() == Challenge.UNKNOWN || challenge.getStatus() == Challenge.SHOWN)) {
      Log.d(TAG, "Ignore initial alarm notification as challenge not shown");
      return;
    }

    Log.d(TAG, "Show initial alarm notification");

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("New challenge available")
            .setContentText(challenge.getContent())
            .setAutoCancel(true);
    Intent resultIntent = new Intent(context, MainActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    notificationManager.notify(NOTIFICATION_ID_INITIAL, mBuilder.build());
  }

  public void showFinalAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      // Show notification only for accepted challenge.
      Log.d(TAG, "Ignore final alarm notification as challenge not accepted");
      return;
    }

    Log.d(TAG, "Show final alarm notification");

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Finish your challenge")
            .setContentText(challenge.getContent())
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
    notificationManager.notify(NOTIFICATION_ID_FINAL, mBuilder.build());
  }

  public void showReminderAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      Log.d(TAG, "Ignore reminder alarm notification as challenge not accepted");
      return;
    }

    Log.d(TAG, "Show reminder alarm notification");

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Remember about your challenge")
            .setContentText(challenge.getContent())
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
    notificationManager.notify(NOTIFICATION_ID_REMINDER, mBuilder.build());
  }

  public void cancelInitialNotification() {
    notificationManager.cancel(NOTIFICATION_ID_INITIAL);
  }

  public void cancelReminderNotification() {
    notificationManager.cancel(NOTIFICATION_ID_REMINDER);
  }
}
