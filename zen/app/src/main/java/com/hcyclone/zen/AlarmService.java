package com.hcyclone.zen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

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

public final class AlarmService implements SharedPreferences.OnSharedPreferenceChangeListener {

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

  public void init(@NonNull Context context) {
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

    PreferenceManager.getDefaultSharedPreferences(context)
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PreferencesService.PREF_KEY_INITIAL_ALARM_LIST.equals(key)) {
      setInitialAlarm();
    } else if (PreferencesService.PREF_KEY_FINAL_ALARM_LIST.equals(key)) {
      setFinalAlarm();
    } else if (PreferencesService.PREF_KEY_REMINDER_ALARM_LIST.equals(key)) {
      setReminderAlarm();
    }
  }

  /**
   * Service alarm is fired between 3-5am every day.
   */
  public void setServiceAlarm() {
    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 10_000;
    } else {
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      // Random hour between 3 and 4.
      int hour = (int) (Math.random() * 2) + 3;
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      // Add random minutes to hours.
      int minute = (int) (Math.random() * 60);
      calendar.add(Calendar.MINUTE, minute);
    }
    long alarmStartTime = calendar.getTimeInMillis();
    Log.d(TAG, "Set service alarm to " + calendar.getTime());
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
        serviceAlarmPengingIntent);
  }

  public void setInitialAlarm() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST, null);
    if (context.getResources().getString(R.string.pref_time_never).equals(summary)) {
      Log.d(TAG, "Initial alarm disabled");
      alarmManager.cancel(initialAlarmPengingIntent);
      return;
    }

    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    int hour = 0;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
    } else {
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      if (context.getResources().getString(R.string.pref_time_random).equals(summary)) {
        hour = (int) (Math.random() * 7) + 6;
      } else {
        hour = Integer.parseInt(summary);
      }
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    long alarmStartTime = calendar.getTimeInMillis();
    Log.d(TAG, "Set initial alarm hour to " + hour);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
        initialAlarmPengingIntent);
  }

  public void setFinalAlarm() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_FINAL_ALARM_LIST, null);
    if (context.getResources().getString(R.string.pref_time_never).equals(summary)) {
      Log.d(TAG, "Final alarm disabled");
      alarmManager.cancel(finalAlarmPengingIntent);
      return;
    }

    Log.d(TAG, "Set final alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    int hour = 0;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
    } else {
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      if (context.getResources().getString(R.string.pref_time_random).equals(summary)) {
        hour = (int) (Math.random() * 7) + 18;
      } else {
        hour = Integer.parseInt(summary);
      }
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    long alarmStartTime = calendar.getTimeInMillis();
    Log.d(TAG, "Set final alarm hour to " + hour);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
        finalAlarmPengingIntent);
  }

  public void setReminderAlarm() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_REMINDER_ALARM_LIST, null);
    if (context.getResources().getString(R.string.pref_time_never).equals(summary)) {
      Log.d(TAG, "Reminder alarm disabled");
      alarmManager.cancel(reminderAlarmPengingIntent);
      return;
    }

    long alarmRepeatTime;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
    } else {
      alarmRepeatTime = AlarmManager.INTERVAL_HOUR * Integer.parseInt(summary);
    }
    long alarmStartTime = new Date().getTime();
    Log.d(TAG, "Set reminder repeat alarm to " + summary);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
        reminderAlarmPengingIntent);
  }

  public void stopReminderAlarm() {
    alarmManager.cancel(reminderAlarmPengingIntent);
  }
}
