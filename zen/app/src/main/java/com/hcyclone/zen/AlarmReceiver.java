package com.hcyclone.zen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;

import android.app.PendingIntent;
import android.app.NotificationManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

  private static final String TAG = AlarmReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    int alarmId = intent.getIntExtra(AlarmService.PARAM_ID, 0);
    Log.d(TAG, "Alarm received with id: " + alarmId);
    switch (alarmId) {
      case AlarmService.ALARM_CODE_SERVICE:
        context.startService(new Intent(context, FirebaseService.class));
        AlarmService.getInstance().updateServiceAlarm();
        break;
      case AlarmService.ALARM_CODE_INITIAL:
        showInitialAlarmNotification(context);
        AlarmService.getInstance().updateInitialAlarm();
        break;
      case AlarmService.ALARM_CODE_FINAL:
        showFinalAlarmNotification(context);
        AlarmService.getInstance().updateFinalAlarm();
        AlarmService.getInstance().stopReminderAlarm();
        break;
      case AlarmService.ALARM_CODE_REMINDER:
        showReminderAlarmNotification(context);
        break;
    }
  }

  private void showInitialAlarmNotification(Context context) {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (!(challenge.getStatus() == Challenge.UNKNOWN || challenge.getStatus() == Challenge.SHOWN)) {
      Log.d(TAG, "Ignore initial alarm notification as challenge not shown");
      return;
    }

    Log.d(TAG, "Show initial alarm notification");
    // TODO: add action.
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("New challenge available")
            .setContentText(challenge.getContent());
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    int mNotificationId = 1;
    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotifyMgr.notify(mNotificationId, mBuilder.build());
  }

  private void showFinalAlarmNotification(Context context) {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      // Show notification only for accepted challenge.
      Log.d(TAG, "Ignore final alarm notification as challenge not accepted");
      return;
    }

    Log.d(TAG, "Show final alarm notification");

    // TODO: add action.
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Finish your challenge")
            .setContentText(challenge.getContent());
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    int mNotificationId = 2;
    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotifyMgr.notify(mNotificationId, mBuilder.build());
  }

  private void showReminderAlarmNotification(Context context) {
    Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      Log.d(TAG, "Ignore reminder alarm notification as challenge not accepted");
      return;
    }

    Log.d(TAG, "Show reminder alarm notification");

    // TODO: add action.
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Remember about your challenge")
            .setContentText(challenge.getContent());
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    int mNotificationId = 3;
    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotifyMgr.notify(mNotificationId, mBuilder.build());
  }
}
