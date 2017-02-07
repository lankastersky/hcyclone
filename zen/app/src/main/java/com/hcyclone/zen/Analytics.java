package com.hcyclone.zen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Google analytics facade.
 */
public class Analytics {

  @VisibleForTesting
  static Analytics instance = new Analytics();

  private Tracker tracker;

  private Analytics() {}

  public static Analytics getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
    tracker = analytics.newTracker(R.xml.global_tracker);
  }

  public void sendChallengeUpdate(@NonNull Challenge challenge) {
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
        .setCategory("Challenge Update")
        .setAction(action)
        .setLabel(challenge.getId())
        .build());
  }
}
