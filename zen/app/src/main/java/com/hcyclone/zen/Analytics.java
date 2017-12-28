package com.hcyclone.zen;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hcyclone.zen.model.Challenge;

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
    send("Challenge Status Update", action, challenge.getId());
  }

  public void sendChallengeRating(@NonNull Challenge challenge) {
    send("Challenge Rating Update", "rated", challenge.getId(), (long) challenge.getRating());
  }

  public void sendChallengeComments(String comments) {
    send("Challenge Comments", "comment", comments != null ? comments.length() : 0);
  }

  public void sendSettings(@NonNull String action, String value) {
    send("Settings Update", action, value);
  }

  public void sendLevelUp(int value) {
    send("Level Up", "Level Up", String.valueOf(value));
  }

  public void sendFilterChallenges(String action, String value) {
    send("Filter challenges", action, value);
  }

  public void sendStatisticsChart(String value) {
    send("Statistics chart", "View", value);
  }

  public void sendShare(String value) {
    send("Share challenge", "share", value);
  }

  private void send(String category, String action, String label) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory(category)
        .setAction(action)
        .setLabel(label)
        .build());
  }

  private void send(String category, String action, long value) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory(category)
        .setAction(action)
        .setValue(value)
        .build());
  }

  private void send(String category, String action, String label, long value) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory(category)
        .setAction(action)
        .setLabel(label)
        .setValue(value)
        .build());
  }
}
