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

  /**
   * Service alarm is fired at 2am every night.
   */
  public void setServiceAlarm() {
    // TODO: check if enabled in preferences.
    Log.d(TAG, "Set service alarm");

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    long alarmStartTime;
    if (BuildConfig.DEBUG) {
      calendar.add(Calendar.SECOND, 10);
      alarmStartTime = calendar.getTimeInMillis();
    } else {
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 2);
      alarmStartTime = calendar.getTimeInMillis();
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, serviceAlarmPengingIntent);
  }

  public void setInitialAlarm() {
    // TODO: check if enabled in preferences.
    Log.d(TAG, "Set initial alarm");

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    long alarmStartTime;
    if (BuildConfig.DEBUG) {
      calendar.add(Calendar.SECOND, 5);
      alarmStartTime = calendar.getTimeInMillis();
    } else {
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 6);
      alarmStartTime = calendar.getTimeInMillis();
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, initialAlarmPengingIntent);
  }

  public void setFinalAlarm() {
    // TODO: check if enabled in preferences.
    Log.d(TAG, "Set final alarm");

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    long alarmStartTime;
    if (BuildConfig.DEBUG) {
      calendar.add(Calendar.SECOND, 5);
      alarmStartTime = calendar.getTimeInMillis();
    } else {
      // TODO: add randomness.
      calendar.set(Calendar.HOUR_OF_DAY, 18);
      alarmStartTime = calendar.getTimeInMillis();
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, finalAlarmPengingIntent);
  }

  public void setReminderAlarm() {
    // TODO: check if enabled in preferences.
    Log.d(TAG, "Set reminder alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmStartTime;
    long alarmRepeatTime;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
      alarmStartTime = calendar.getTimeInMillis();
    } else {
      // TODO: add randomness.
      alarmRepeatTime = AlarmManager.INTERVAL_HOUR;
      calendar.set(Calendar.HOUR_OF_DAY, 1);
      alarmStartTime = calendar.getTimeInMillis();
    }

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime,
        alarmRepeatTime, reminderAlarmPengingIntent);
  }

  public void stopReminderAlarm() {
    alarmManager.cancel(reminderAlarmPengingIntent);
  }
}
