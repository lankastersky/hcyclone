package com.hcyclone.zen;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public final class NotificationService {

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

  public void init(@NonNull Context context) {
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
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(text)
            .setLights(Color.RED, 3000, 3000)
            .setAutoCancel(true);
    Intent resultIntent = new Intent(context, MainActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    builder.setContentIntent(resultPendingIntent);
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (sharedPreferences.getBoolean(PreferencesService.PREF_KEY_NOTIFICATION_VIBRATE, false)) {
      builder.setVibrate(new long[] { 0, 50, 200, 50, 200, 50 });
    }
    String ringtoneUri = sharedPreferences.getString(
        PreferencesService.PREF_KEY_NOTIFICATION_RINGTONE, null);
    if (ringtoneUri != null) {
      Uri uri = Uri.parse(ringtoneUri);
      builder.setSound(uri);
    }
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }
}
