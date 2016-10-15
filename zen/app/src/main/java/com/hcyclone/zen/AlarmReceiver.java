package com.hcyclone.zen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;

import android.app.PendingIntent;
import android.app.NotificationManager;

public class AlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    switch (intent.getIntExtra(AlarmService.PARAM_ID, 0)) {
      case AlarmService.ALARM_CODE_NIGHTLY:
        context.startService(new Intent(context, FirebaseService.class));
        break;
      case AlarmService.ALARM_CODE_INITIAL:
        showInitialAlarmNotification(context);
        AlarmService.getInstance().updateInitialAlarm();
        break;
    }
  }

  private void showInitialAlarmNotification(Context context) {
    // TODO: show current challenge.
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("New challenge available")
            .setContentText("Let's start!");
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    int mNotificationId = 001;
    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    mNotifyMgr.notify(mNotificationId, mBuilder.build());
  }
}
