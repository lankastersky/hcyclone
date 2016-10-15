package com.hcyclone.zen;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;

import android.util.Log;

import java.util.Calendar;

/*
start app (1st time)
    create daily repeatable alarm with firetime (at night) if not yet
        decline current challenge if needed
        go to server and gets challenges
        ask model for next challenge
        generates accept time, stores it

When fired daily alarm
    decline current challenge if needed
    go to server and get challenges
    ask model for next challenge
    generates accept time, stores it

create initial alarm:

when reboot before daily alarm; before initial alarm; before final alarm
    create daily alarm
    create initial alarm if daily is not fired today
    create repeatable alarm if accepted
    create final alarm if accepted

when accept
    delete initial alarm
    set repeatable constant alarm for stored challenge
    create final alarm for this challenge with evening time

when finish or decline
    delete repeatable alarm
    delete final alarm if not yet

when app runs in background during daily alarm; initial alarm; repeatable alarm; final alarm
*/

public final class AlarmService {

  private static final String TAG = AlarmService.class.getSimpleName();

  public static final String PARAM_ID = "id";
  public static final int ALARM_CODE_NIGHTLY = 1;
  public static final int ALARM_CODE_INITIAL = 2;

  private static final AlarmService instance = new AlarmService();
  private Context context;
  private PendingIntent nightlyAlarmPengingIntent;
  private PendingIntent initialAlarmPengingIntent;

  private AlarmService() {
  }

  public static AlarmService getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;

    Intent nightlyAlarmIntent = new Intent(context, AlarmReceiver.class);
    nightlyAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_NIGHTLY);
    nightlyAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_NIGHTLY,
        nightlyAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent initialAlarmIntent = new Intent(context, AlarmReceiver.class);
    initialAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_INITIAL);
    initialAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_INITIAL,
        initialAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private boolean isAlarmSet(int code) {
    return (PendingIntent.getBroadcast(context, code,
        new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
  }

  /**
   * Nightly alarm is fired at 2am every night.
   */
  public void createNightlyAlarmIfNeeded() {
    if (isAlarmSet(ALARM_CODE_NIGHTLY)) {
      Log.d(TAG, "Nightly alarm is already active");
      return;
    }

    Log.d(TAG, "Set nightly alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (BuildConfig.DEBUG) {
      alarmTime = 5_000;
    } else {
      calendar.setTimeInMillis(System.currentTimeMillis());
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 2);
      alarmTime = calendar.getTimeInMillis();
    }

    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, nightlyAlarmPengingIntent);
  }

  public void createInitialAlarmIfNeeded() {
    if (isAlarmSet(ALARM_CODE_INITIAL)) {
      Log.d(TAG, "Initial alarm is already active");
      return;
    }

    Log.d(TAG, "Set initial alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (BuildConfig.DEBUG) {
      alarmTime = 5_000;
    } else {
      calendar.setTimeInMillis(System.currentTimeMillis());
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 6);
      alarmTime = calendar.getTimeInMillis();
    }

    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, initialAlarmPengingIntent);
  }

  public void updateInitialAlarm() {
    // TODO: add randomness for the next day.
  }
}
