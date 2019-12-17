package com.hcyclone.zyq;

import android.content.Context;

import androidx.annotation.NonNull;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hcyclone.zyq.model.Exercise.LevelType;
import com.hcyclone.zyq.model.Exercise.ExerciseType;

/**
 * Google analytics facade.
 */
public class Analytics {

  private static final Analytics instance = new Analytics();

  private Tracker tracker;

  private Analytics() {}

  public static Analytics getInstance() {
    return instance;
  }

  void init(@NonNull Context context) {
    GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
    tracker = analytics.newTracker(R.xml.global_tracker);
  }

  public void sendExercise(String exerciseId) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Exercise")
        .setAction("View")
        .setLabel(exerciseId)
        .build());
  }

  public void sendExerciseLevel(LevelType level) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Practice Level")
        .setAction("View")
        .setLabel(String.valueOf(level.getValue()))
        .build());
  }

  public void sendExerciseType(ExerciseType type) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Practice Type")
        .setAction("View")
        .setLabel(String.valueOf(type.getValue()))
        .build());
  }

  public void sendAudio(String audioName) {
    tracker.send(new HitBuilders.EventBuilder()
        .setCategory("Audio")
        .setAction("Listen")
        .setLabel(audioName)
        .build());
  }
}
