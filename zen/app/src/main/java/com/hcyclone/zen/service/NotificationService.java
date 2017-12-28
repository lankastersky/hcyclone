package com.hcyclone.zen.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.view.MainActivity;

public final class NotificationService implements OnSharedPreferenceChangeListener {

  private static final String TAG = NotificationService.class.getSimpleName();

  private static final NotificationService instance = new NotificationService();

  private static final int NOTIFICATION_ID = 1;
  private static final String CHALLENGE_CHANNEL_ID = "challenge_channel";
  private static final int LIGHT_TIME_MS = 3000;

  private NotificationManager notificationManager;
  private Context context;
  private SharedPreferences sharedPreferences;

  private NotificationService() {}

  public static NotificationService getInstance() {
    return instance;
  }

  private static int getNotificationIcon() {
    boolean useBlackIcon = (android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.LOLLIPOP);
    return useBlackIcon ? R.mipmap.ic_menu_challenge : R.mipmap.ic_menu_challenge_white;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    notificationManager = (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);

    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    PreferenceManager.getDefaultSharedPreferences(context)
        .registerOnSharedPreferenceChangeListener(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      buildChannel();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PreferencesService.PREF_KEY_NOTIFICATION_VIBRATE.equals(key)) {
      String value = String.valueOf(sharedPreferences.getBoolean(key, true));
      Analytics.getInstance().sendSettings(Analytics.SETTINGS_UPDATE_NOTIFICATION_VIBRATE,
          value);
    }
  }

  void showInitialAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (challenge == null) {
      Log.e(TAG, "Ignore initial alarm notification as challenge is null");
      return;
    }
    if (!(challenge.getStatus() == Challenge.UNKNOWN || challenge.getStatus() == Challenge.SHOWN)) {
      Log.d(TAG, "Ignore initial alarm notification as challenge is not shown");
      return;
    }
    Log.d(TAG, "Show initial alarm notification");
    showNotification(context.getString(R.string.notification_challenge_start),
        challenge.getContent());
  }

  void showFinalAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (challenge == null) {
      Log.e(TAG, "Ignore final alarm notification as challenge is null");
      return;
    }
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      // Show notification only for accepted challenge.
      Log.d(TAG, "Ignore final alarm notification as challenge not accepted");
      return;
    }
    Log.d(TAG, "Show final alarm notification");
    showNotification(context.getString(R.string.notification_challenge_finish),
        challenge.getContent());
  }

  void showDailyAlarmNotification() {
    Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
    if (challenge == null) {
      Log.e(TAG, "Ignore daily alarm notification as challenge is null");
      return;
    }
    if (challenge.getStatus() != Challenge.ACCEPTED) {
      Log.d(TAG, "Ignore daily alarm notification as challenge not accepted");
      return;
    }
    Log.d(TAG, "Show daily alarm notification");
    showNotification(challenge.getContent(), challenge.getDetails());
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private NotificationChannel buildChannel() {
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    // The user-visible name of the channel.
    CharSequence name = context.getString(R.string.challenge_current);
    // The user-visible description of the channel.
    //String description = "Media playback controls";
    int importance = NotificationManager.IMPORTANCE_LOW;
    NotificationChannel channel = new NotificationChannel(CHALLENGE_CHANNEL_ID, name, importance);
    // Configure the notification channel.
    //channel.setDescription(description);
    //channel.setShowBadge(false);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManager.createNotificationChannel(channel);
    return channel;
  }

  private void showNotification(String title, String text) {
    if (AppLifecycleManager.isAppVisible()) {
      return;
    }

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context, CHALLENGE_CHANNEL_ID)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setLights(Color.RED, LIGHT_TIME_MS, LIGHT_TIME_MS)
            .setAutoCancel(true);
    Intent resultIntent = new Intent(context, MainActivity.class);
    resultIntent.putExtra(MainActivity.INTENT_PARAM_START_FROM_NOTIFICATION, true);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    builder.setContentIntent(resultPendingIntent);
    if (sharedPreferences.getBoolean(PreferencesService.PREF_KEY_NOTIFICATION_VIBRATE, false)) {
      builder.setVibrate(new long[] { 0, 50, 200, 50, 200, 50 });
    }
    String ringtoneUri = sharedPreferences.getString(
        PreferencesService.PREF_KEY_NOTIFICATION_RINGTONE, null);
    Uri soundUri;
    if (!TextUtils.isEmpty(ringtoneUri)) {
      soundUri = Uri.parse(ringtoneUri);
    } else {
      soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }
    builder.setSound(soundUri);
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }
}
