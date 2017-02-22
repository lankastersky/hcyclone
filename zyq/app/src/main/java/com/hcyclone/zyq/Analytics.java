package com.hcyclone.zyq;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Google analytics facade.
 */
public class Analytics {

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


//  public void sendSettings(@NonNull String action, String value) {
//    tracker.send(new HitBuilders.EventBuilder()
//        .setCategory("Settings Update")
//        .setAction(action)
//        .setLabel(value)
//        .build());
//  }
//
//  public void sendLevelUp(int value) {
//    tracker.send(new HitBuilders.EventBuilder()
//        .setCategory("Level Up")
//        .setAction("Level Up")
//        .setLabel(String.valueOf(value))
//        .build());
//  }
}
