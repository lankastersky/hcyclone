package com.hcyclone.zen;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hcyclone.zen.model.Challenge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Google analytics facade. */
public class Analytics {

  private static final DateFormat CHALLENGES_TIME_DATE_FORMAT =
      new SimpleDateFormat("yyyy.MM.dd", Locale.US);

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

  public void sendChallengeRating(Challenge challenge) {
    send("Challenge Rating Update", "rated", challenge.getId(), (long) challenge.getRating());
  }

  public void sendChallengeComments(Challenge challenge) {
    send(
        "Challenge Comments",
        "comment",
        challenge.getId(),
        challenge.getComments() != null ? challenge.getComments().length() : 0);
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

  public void sendChangeLanguage(String value) {
    send("Change language", "change", value);
  }

  public void sendChallengeBackupData(Challenge challenge) {
    send("Challenge backup", "Statuses", challenge.getPrevStatuses().toString());
    List<String> finishedTimesAsString = new ArrayList<>();
    for (long time : challenge.getPrevFinishedTimes()) {
      finishedTimesAsString.add(Utils.timeToString(time, CHALLENGES_TIME_DATE_FORMAT));
    }
    send("Challenge backup", "Finished times", finishedTimesAsString.toString());
    send("Challenge backup", "Ratings", challenge.getPrevRatings().toString());
  }

  public void sendBuyExtendedVersion() {
    send("Purchase", "Buy", "Premium version");
  }

  public void sendSubscribeOnExtendedVersion() {
    send("Purchase", "Subscribe", "Premium version");
  }

  public void sendScreen(String dialogName) {
    tracker.setScreenName(dialogName);
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  private void send(String category, String action, String label) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory(category)
        .setAction(action)
        .setLabel(label)
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
