package com.hcyclone.zyq.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.text.format.DateUtils;
import android.view.KeyEvent;

import com.hcyclone.zyq.App;
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;

import org.joda.time.Duration;

import java.io.IOException;

/**
 * Plays music in background. Starts as a foreground service.
 */
public class AudioService extends Service {

  private static final String TAG = AudioService.class.getSimpleName();
  private static final int AUDIO_NOTIFICATION_ID = 1;
  private static final Duration UPDATE_NOTIFICATION_INTERVAL = Duration.standardSeconds(1);

  private AudioPlayer player;
  private Handler updateNotificationHandler;

  private final Runnable updateNotificationRunnable = new Runnable() {
    @Override
    public void run() {
      if (player == null) {
        return;
      }
      if (player.isPlaying()) {
        updateNotification();
      }
      updateNotificationHandler.postDelayed(
          updateNotificationRunnable, UPDATE_NOTIFICATION_INTERVAL.getMillis());
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    App app = (App) getApplication();
    player = app.getPlayer();

    // TODO: support version O.
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//      //createChannel();
//    }

    updateNotificationHandler = new Handler();
  }

  public int onStartCommand(Intent intent, int flags, final int startId) {
    Bundle bundle = intent.getExtras();
    KeyEvent keyEvent = bundle.getParcelable(Intent.EXTRA_KEY_EVENT);
    if (keyEvent != null) {
      int keyCode = keyEvent.getKeyCode();
      switch (keyCode) {
        case KeyEvent.KEYCODE_MEDIA_STOP:
          stopSelf(startId);
          return START_NOT_STICKY;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          player.pause();
          updateNotification();
          return START_NOT_STICKY;
        default:
          throw new AssertionError("Wrong key event: " + keyCode);
      }
    }

    String audioName = bundle.getString(BundleConstants.AUDIO_NAME_KEY);

    try {
      player.play(audioName, new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          Log.d(TAG, "Completed playing audio");
          stopSelf(startId);
        }
      });
      startForeground(AUDIO_NOTIFICATION_ID, buildNotification());
    } catch (IOException e) {
      Log.e(AudioService.TAG, "Failed to play audio", e);
      return START_NOT_STICKY;
    }

    updateNotificationHandler.post(updateNotificationRunnable);
    return START_REDELIVER_INTENT;
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    player.reset();
    player = null;
  }

  private void updateNotification() {
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(
        AUDIO_NOTIFICATION_ID, buildNotification());
  }

  private Notification buildNotification() {
    PendingIntent playIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
        this, PlaybackStateCompat.ACTION_PLAY);
    PendingIntent pauseIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
        this, PlaybackStateCompat.ACTION_PAUSE);
    PendingIntent stopIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
        this, PlaybackStateCompat.ACTION_STOP);

    int currentTime = player.getCurrentPosition();
    String currentTimeHuman = DateUtils.formatElapsedTime(currentTime / 1000);

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, "channel");
    notificationBuilder
        .setStyle(
            new MediaStyle()
//                      .setMediaSession(token)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_STOP)))
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setSmallIcon(R.mipmap.ic_play_arrow_white_24dp)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(player.getCurlurrentAudioName())
        .setSubText(String.valueOf(currentTimeHuman))
        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.play_launcher))
//              .setContentIntent(createContentIntent())
        .setDeleteIntent(stopIntent)
        .addAction(R.mipmap.ic_stop_white_24dp, "Stop", stopIntent);
        //.setAutoCancel(true)

    if (player.isPlaying()) {
      notificationBuilder.addAction(R.mipmap.ic_pause_white_24dp, "Pause", pauseIntent);
    } else {
      notificationBuilder.addAction(R.mipmap.ic_play_arrow_white_24dp, "Play", playIntent);
    }

    return notificationBuilder.build();
  }
}
