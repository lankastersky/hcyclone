package com.hcyclone.zen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

public final class AlarmService implements OnSharedPreferenceChangeListener {

  private static final String TAG = AlarmService.class.getSimpleName();

  public static final String PARAM_ID = "alarm_id";
  public static final int ALARM_CODE_SERVICE = 1;
  public static final int ALARM_CODE_INITIAL = 2;
  public static final int ALARM_CODE_FINAL = 3;
  public static final int ALARM_CODE_REMINDER = 4;

  private static final AlarmService instance = new AlarmService();

  private Context context;
  private SharedPreferences sharedPreferences;
  private AlarmManager alarmManager;
  private PendingIntent serviceAlarmPengingIntent;
  private PendingIntent initialAlarmPengingIntent;
  private PendingIntent finalAlarmPengingIntent;
  private PendingIntent reminderAlarmPengingIntent;

  private AlarmService() {}

  public static AlarmService getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

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

  public void setAlarms() {
    setServiceAlarm();
    setInitialAlarm();
    setFinalAlarm();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    String action = null;
    String value = null;

    if (PreferencesService.PREF_KEY_INITIAL_ALARM_LIST.equals(key)) {
      setInitialAlarm();
      String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
      value = sharedPreferences.getString(key, prefTimeRandom);
      action = Analytics.SETTINGS_UPDATE_INITIAL_ALARM;
    } else if (PreferencesService.PREF_KEY_FINAL_ALARM_LIST.equals(key)) {
      setFinalAlarm();
      String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
      value = sharedPreferences.getString(key, prefTimeRandom);
      action = Analytics.SETTINGS_UPDATE_FINAL_ALARM;
    } else if (PreferencesService.PREF_KEY_REMINDER_ALARM_LIST.equals(key)) {
      setReminderAlarm();
      String prefTimeEveryHour = "1";
      value = sharedPreferences.getString(key, prefTimeEveryHour);
      action = Analytics.SETTINGS_UPDATE_REMINDER_ALARM;
    } else if (PreferencesService.PREF_KEY_SHOW_NOTIFICATION.equals(key)) {
      setAlarms();
      value = String.valueOf(areAlarmsEnabled());
      action = Analytics.SETTINGS_UPDATE_SHOW_NOTIFICATION;
    }
    if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(value)) {
      Analytics.getInstance().sendSettings(action, value);
    }
  }

  private boolean areAlarmsEnabled() {
    return sharedPreferences.getBoolean(PreferencesService.PREF_KEY_SHOW_NOTIFICATION, true);
  }

  /**
   * Service alarm is fired between 3-5am every day.
   */
  public void setServiceAlarm() {
    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
    } else {
      Date midnight = Utils.getMidnight(calendar.getTimeInMillis());
      calendar.setTime(midnight);
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      // Random hour between 3 and 4.
      int hour = (int) (Math.random() * 2) + 3;
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      // Add random minutes to hours.
      int minute = (int) (Math.random() * 60);
      calendar.add(Calendar.MINUTE, minute);
    }
    long alarmStartTime = calendar.getTimeInMillis();
//    Log.d(TAG, "Set service alarm to " + calendar.getTime());
//    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
//        serviceAlarmPengingIntent);
    Log.d(TAG, "Set service alarm to " + new Date(alarmStartTime + alarmRepeatTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime + alarmRepeatTime,
        serviceAlarmPengingIntent);
  }

  public void setInitialAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST, prefTimeRandom);
    if (!areAlarmsEnabled() || prefTimeNever.equals(summary)) {
      Log.d(TAG, "Initial alarm disabled");
      alarmManager.cancel(initialAlarmPengingIntent);
      return;
    }

    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    int hour;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
      //calendar.add(Calendar.SECOND, 1);
    } else {
      Date midnight = Utils.getMidnight(calendar.getTimeInMillis());
      calendar.setTime(midnight);
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      if (context.getResources().getString(R.string.pref_time_random).equals(summary)) {
        // Random hour between 6 and 12.
        hour = (int) (Math.random() * 7) + 6;
      } else {
        hour = Integer.parseInt(summary);
      }
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    long alarmStartTime = calendar.getTimeInMillis();
//    Log.d(TAG, "Set initial alarm to " + calendar.getTime());
//    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
//        initialAlarmPengingIntent);
    Log.d(TAG, "Set initial alarm to " + new Date(alarmStartTime + alarmRepeatTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime + alarmRepeatTime,
        initialAlarmPengingIntent);
  }

  public void setFinalAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_FINAL_ALARM_LIST, prefTimeRandom);
    if (!areAlarmsEnabled() || prefTimeNever.equals(summary)) {
      Log.d(TAG, "Final alarm disabled");
      alarmManager.cancel(finalAlarmPengingIntent);
      return;
    }

    Calendar calendar = Calendar.getInstance();
    long alarmRepeatTime;
    int hour;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
      //calendar.add(Calendar.SECOND, 1);
    } else {
      Date midnight = Utils.getMidnight(calendar.getTimeInMillis());
      calendar.setTime(midnight);
      alarmRepeatTime = AlarmManager.INTERVAL_DAY;
      if (context.getResources().getString(R.string.pref_time_random).equals(summary)) {
        // Random hour between 18 and 24.
        hour = (int) (Math.random() * 7) + 18;
      } else {
        hour = Integer.parseInt(summary);
      }
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    long alarmStartTime = calendar.getTimeInMillis();
//    Log.d(TAG, "Set final alarm to " + calendar.getTime());
//    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
//        finalAlarmPengingIntent);
    Log.d(TAG, "Set final alarm to " + new Date(alarmStartTime + alarmRepeatTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime + alarmRepeatTime,
        finalAlarmPengingIntent);
  }

  public void setReminderAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeEveryHour = "1";
    String summary = sharedPreferences.getString(
        PreferencesService.PREF_KEY_REMINDER_ALARM_LIST, prefTimeEveryHour);
    if (!areAlarmsEnabled() || prefTimeNever.equals(summary)) {
      Log.d(TAG, "Reminder alarm disabled");
      stopReminderAlarm();
      return;
    }

    long alarmRepeatTime;
    if (BuildConfig.DEBUG) {
      alarmRepeatTime = 5_000;
    } else {
      alarmRepeatTime = AlarmManager.INTERVAL_HOUR * Integer.parseInt(summary);
    }
    long alarmStartTime = new Date().getTime();
//    Log.d(TAG, "Set reminder alarm to " + new Date(alarmRepeatTime));
//    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmRepeatTime,
//        reminderAlarmPengingIntent);
    Log.d(TAG, "Set reminder alarm to " + new Date(alarmStartTime + alarmRepeatTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime + alarmRepeatTime,
        reminderAlarmPengingIntent);
  }

  public void stopReminderAlarm() {
    alarmManager.cancel(reminderAlarmPengingIntent);
  }
}
