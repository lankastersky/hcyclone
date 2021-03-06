package com.hcyclone.zyq.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.KeyEvent;

import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;
import com.crashlytics.android.Crashlytics;
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
  private static final int AUDIO_NOTIFICATION_ID = 101;
  private static final String AUDIO_CHANNEL_ID = "audio_channel";
  private static final Duration UPDATE_NOTIFICATION_INTERVAL = Duration.standardSeconds(1);

  public static final String ACTION_PREPARED_AUDIO = "com.hcyclone.zyq.service.action.PREPARED_AUDIO";

  private AudioPlayer player;
  private Handler updateNotificationHandler;
  private NotificationManager notificationManager;

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
    Log.d(TAG, "onCreate");
    super.onCreate();

    App app = (App) getApplication();
    player = app.getPlayer();

    updateNotificationHandler = new Handler();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      buildChannel();
    }
    startForeground(AUDIO_NOTIFICATION_ID, buildNotification());
  }

  public int onStartCommand(Intent intent, int flags, final int startId) {
    Log.d(TAG, "onStartCommand");
    KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
    if (keyEvent != null) {
      int keyCode = keyEvent.getKeyCode();
      switch (keyCode) {
        case KeyEvent.KEYCODE_MEDIA_STOP:
          Log.d(TAG, "Stop");
          if (!intent.getBooleanExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, false)) {
            sendAudioBroadcast(KeyEvent.KEYCODE_MEDIA_STOP);
          }
          deleteNotification();
          stopSelf();
          return START_NOT_STICKY;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          Log.d(TAG, "Pause");
          player.pause();
          if (!intent.getBooleanExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, false)) {
            sendAudioBroadcast(KeyEvent.KEYCODE_MEDIA_PAUSE);
          }
          updateNotification();
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          Log.d(TAG, "Play");
          // Can be empty when called from notification.
          String audioName = intent.getStringExtra(BundleConstants.AUDIO_NAME_KEY);
          try {
            play(audioName);
          } catch (IOException e) {
            Crashlytics.logException(e);
            Log.e(AudioService.TAG, "Failed to play audio", e);
            break;
          }
          updateNotificationHandler.post(updateNotificationRunnable);
          // Avoid broadcasting back to playback controls.
          if (!intent.getBooleanExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, false)) {
            sendAudioBroadcast(KeyEvent.KEYCODE_MEDIA_PLAY);
          }
          break;
        default:
          throw new AssertionError("Wrong key event: " + keyCode);
      }
    }
    return START_STICKY;
  }

  private void play(String audioName) throws IOException {
    if (player.isInitied()) { // Continue playing what we played.
        player.play();
        return;
    }
    player.play(audioName,
        (mp) -> {
          Log.d(TAG, "Prepared playing audio");
          sendActionBroadcast(ACTION_PREPARED_AUDIO);
        },
        (mp) -> {
          Log.d(TAG, "Completed playing audio");
          stopSelf();
        },
        (MediaPlayer mediaPlayer, int i, int i1) -> {
          Log.d(TAG, "Failed playing audio");
          stopSelf();
          return true;
        });
  }

  private void sendAudioBroadcast(int keyEvent) {
    Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(BundleConstants.AUDIO_BROADCAST_RECEIVER_ACTION);
    broadcastIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
    sendBroadcast(broadcastIntent);
  }

  private void sendActionBroadcast(String action) {
    Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(action);
    sendBroadcast(broadcastIntent);
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      stopForeground(true);
    }
    player.reset();
    player = null;
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private NotificationChannel buildChannel() {
    // The user-visible name of the channel.
    CharSequence name = "Media playback";
    // The user-visible description of the channel.
    String description = "Media playback controls";
    int importance = NotificationManager.IMPORTANCE_LOW;
    NotificationChannel channel = new NotificationChannel(AUDIO_CHANNEL_ID, name, importance);
    // Configure the notification channel.
    channel.setDescription(description);
    channel.setShowBadge(false);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManager.createNotificationChannel(channel);
    return channel;
  }

  private void updateNotification() {
    notificationManager.notify(AUDIO_NOTIFICATION_ID, buildNotification());
  }

  private void deleteNotification() {
    notificationManager.cancel(AUDIO_NOTIFICATION_ID);
  }

  /* See example http://shortn/_5pmxBDnjGC . */
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
        new NotificationCompat.Builder(this, AUDIO_CHANNEL_ID);
    notificationBuilder
        .setStyle(
            new MediaStyle()
                // TODO: support media sessions http://shortn/_rH8MR9yU35 .
                //.setMediaSession(token)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_STOP)))
        //.setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setSmallIcon(R.mipmap.ic_play_arrow_white_24dp)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(player.getCurrentAudioName())
        .setSubText(String.valueOf(currentTimeHuman))
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.play_launcher))
        .setDeleteIntent(stopIntent);

    if (player.isPlaying()) {
      notificationBuilder.addAction(R.mipmap.ic_pause_white_24dp, "Pause", pauseIntent);
    } else {
      notificationBuilder.addAction(R.mipmap.ic_play_arrow_white_24dp, "Play", playIntent);
    }
    notificationBuilder.addAction(R.mipmap.ic_stop_white_24dp, "Stop", stopIntent);

    return notificationBuilder.build();
  }
}
