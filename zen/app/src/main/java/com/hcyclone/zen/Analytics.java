package com.hcyclone.zen;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Google analytics facade.
 */
public class Analytics {

  public static final String SETTINGS_UPDATE_INITIAL_ALARM = "Initial alarm";
  public static final String SETTINGS_UPDATE_FINAL_ALARM = "Final alarm";
  public static final String SETTINGS_UPDATE_DAILY_ALARM = "Reminder alarm";
  public static final String SETTINGS_UPDATE_SHOW_NOTIFICATION = "Show notification";
  public static final String SETTINGS_UPDATE_NOTIFICATION_VIBRATE = "Notification vibrate";

  private static Analytics instance = new Analytics();

  private Tracker tracker;

  private Analytics() {}

  public static Analytics getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
    tracker = analytics.newTracker(R.xml.global_tracker);
  }

  public void sendChallengeStatus(@NonNull Challenge challenge) {
    String action;
    switch (challenge.getStatus()) {
      case Challenge.SHOWN:
        action = "shown";
        break;
      case Challenge.ACCEPTED:
        action = "accepted";
        break;
      case Challenge.FINISHED:
        action = "finished";
        break;
      case Challenge.DECLINED:
        action = "declined";
        break;
      default:
        return;
    }
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Challenge Status Update")
        .setAction(action)
        .setLabel(challenge.getId())
        .build());
  }

  public void sendChallengeRating(@NonNull Challenge challenge) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Challenge Rating Update")
        .setAction("rated")
        .setLabel(challenge.getId())
        .setValue((long) challenge.getRating())
        .build());
  }

  public void sendSettings(@NonNull String action, String value) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Settings Update")
        .setAction(action)
        .setLabel(value)
        .build());
  }

  public void sendLevelUp(int value) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Level Up")
        .setAction("Level Up")
        .setLabel(String.valueOf(value))
        .build());
  }
}
