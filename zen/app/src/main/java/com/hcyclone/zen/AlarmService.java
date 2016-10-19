package com.hcyclone.zen;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;

import android.util.Log;

import java.util.Calendar;

/*
start app
    create daily repeatable alarm with firetime (at night) if not yet
    create initial repeatable alarm

When fired daily alarm
    decline current challenge if needed
    go to server and get challenges
    ask model for next challenge

when fired initial repeatable alarm
    show notification if app is not active
    update alarm time with some randomness

when fired final repeatable alarm
    shows notification if app is not active and challenge was accepted
    update alarm time with some randomness

when reboot before daily alarm; before initial alarm; before final alarm
    create daily alarm
    create initial alarm
    create repeatable reminder alarm
    create final alarm

when accept
    stop initial alarm
    start repeatable reminder alarm for stored challenge

when finish or decline
    stop repeatable reminder alarm
    stop final alarm if not yet

when app runs in background during daily alarm; initial alarm; repeatable alarm; final alarm

*/

public final class AlarmService {

  private static final String TAG = AlarmService.class.getSimpleName();

  public static final String PARAM_ID = "alarm_id";
  public static final int ALARM_CODE_SERVICE = 1;
  public static final int ALARM_CODE_INITIAL = 2;
  public static final int ALARM_CODE_FINAL = 3;
  public static final int ALARM_CODE_REMINDER = 4;

  private static final AlarmService instance = new AlarmService();

  private Context context;
  private AlarmManager alarmManager;
  private PendingIntent serviceAlarmPengingIntent;
  private PendingIntent initialAlarmPengingIntent;
  private PendingIntent finalAlarmPengingIntent;
  private PendingIntent reminderAlarmPengingIntent;

  private AlarmService() {
  }

  public static AlarmService getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent serviceAlarmIntent = new Intent(context, AlarmReceiver.class);
    serviceAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_SERVICE);
    serviceAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_SERVICE,
        serviceAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent initialAlarmIntent = new Intent(context, AlarmReceiver.class);
    initialAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_INITIAL);
    initialAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_INITIAL,
        initialAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent finalAlarmIntent = new Intent(context, AlarmReceiver.class);
    finalAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_FINAL);
    finalAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_FINAL,
        finalAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent reminderAlarmIntent = new Intent(context, AlarmReceiver.class);
    reminderAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_REMINDER);
    reminderAlarmPengingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_REMINDER,
        reminderAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private boolean isAlarmSet(int code) {
    return (PendingIntent.getBroadcast(context, code,
        new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
  }

  /**
   * Service alarm is fired at 2am every night.
   */
  public void createServiceAlarmIfNeeded() {
    if (isAlarmSet(ALARM_CODE_SERVICE)) {
      Log.d(TAG, "Service alarm is already active");
      return;
    }

    Log.d(TAG, "Set service alarm");

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

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, serviceAlarmPengingIntent);
  }

  public void updateServiceAlarm() {
    // TODO: check if enabled in preferences.
    // TODO: add randomness for the next day.
  }

  public void createInitialAlarmIfNeeded() {
    // TODO: check if enabled in preferences.
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

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, initialAlarmPengingIntent);
  }

  public void updateInitialAlarm() {
    // TODO: check if enabled in preferences.
    // TODO: add randomness for the next day.
  }

  public void createFinalAlarmIfNeeded() {
    // TODO: check if enabled in preferences.
    if (isAlarmSet(ALARM_CODE_FINAL)) {
      Log.d(TAG, "Final alarm is already active");
      return;
    }

    Log.d(TAG, "Set final alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (BuildConfig.DEBUG) {
      alarmTime = 5_000;
    } else {
      calendar.setTimeInMillis(System.currentTimeMillis());
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 18);
      alarmTime = calendar.getTimeInMillis();
    }

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, finalAlarmPengingIntent);
  }

  public void updateFinalAlarm() {
    // TODO: check if enabled in preferences.
    // TODO: add randomness for the next day.
  }

  public void startReminderAlarmIfNeeded() {
    // TODO: check if enabled in preferences.
    if (isAlarmSet(ALARM_CODE_REMINDER)) {
      Log.d(TAG, "Reminder alarm is already active");
      return;
    }

    Log.d(TAG, "Set reminder alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (BuildConfig.DEBUG) {
      alarmTime = 5_000;
    } else {
      calendar.setTimeInMillis(System.currentTimeMillis());
      // TODO: read from preferences.
      calendar.set(Calendar.HOUR_OF_DAY, 2);
      alarmTime = calendar.getTimeInMillis();
    }

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, reminderAlarmPengingIntent);
  }

  public void stopReminderAlarm() {
    // TODO: test if already cancelled.
    alarmManager.cancel(reminderAlarmPengingIntent);
    // TODO: delete notification if shown.
  }

  public void updateReminderAlarm() {
    // TODO: update with values from preferences.
  }
}
