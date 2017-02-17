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
  public static final int ALARM_CODE_DAILY = 4;

  private static final AlarmService instance = new AlarmService();

  private Context context;
  private SharedPreferences sharedPreferences;
  private AlarmManager alarmManager;
  private PendingIntent serviceAlarmPendingIntent;
  private PendingIntent initialAlarmPendingIntent;
  private PendingIntent finalAlarmPendingIntent;
  private PendingIntent dailyAlarmPendingIntent;

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
    serviceAlarmPendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_SERVICE,
        serviceAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent initialAlarmIntent = new Intent(context, AlarmReceiver.class);
    initialAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_INITIAL);
    initialAlarmPendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_INITIAL,
        initialAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent finalAlarmIntent = new Intent(context, AlarmReceiver.class);
    finalAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_FINAL);
    finalAlarmPendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_FINAL,
        finalAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent dailyAlarmIntent = new Intent(context, AlarmReceiver.class);
    dailyAlarmIntent.putExtra(PARAM_ID, ALARM_CODE_DAILY);
    dailyAlarmPendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE_DAILY,
        dailyAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
    } else if (PreferencesService.PREF_KEY_DAILY_ALARM_LIST.equals(key)) {
      setDailyAlarm();
      String prefTimeEveryHour = "1";
      value = sharedPreferences.getString(key, prefTimeEveryHour);
      action = Analytics.SETTINGS_UPDATE_DAILY_ALARM;
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

  private static long getAlarmTime(int hoursToAddToMidnight) {
    return getAlarmTime(hoursToAddToMidnight, 0);
  }

  private static long getAlarmTime(int hoursToAddToMidnight, int minutesToAddToMidnight) {
    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (Utils.getInstance().isDebug()) {
      alarmTime = calendar.getTimeInMillis() + Utils.getInstance().getDebugAlarmRepeatTime();
    } else {
      Date midnight = Utils.getInstance().getMidnight(calendar.getTimeInMillis());
      calendar.setTime(midnight);
      calendar.set(Calendar.HOUR_OF_DAY, hoursToAddToMidnight);
      calendar.add(Calendar.MINUTE, minutesToAddToMidnight);
      alarmTime = calendar.getTimeInMillis();
    }
    return alarmTime;
  }

  /**
   * Service alarm is fired between 3-5am every day.
   */
  public void setServiceAlarm() {
    // Random hour between 3 and 4.
    int hoursToAdd = (int) (Math.random() * 2) + 3;
    // Add random minutes to hours.
    int minutesToAdd = (int) (Math.random() * 60);
    long alarmTime = getAlarmTime(hoursToAdd, minutesToAdd);
    Log.d(TAG, "Set service alarm to " + new Date(alarmTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, serviceAlarmPendingIntent);
  }

  /**
   * Is fired every day between 6 and 12.
   */
  public void setInitialAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
    String settingsHours = sharedPreferences.getString(
        PreferencesService.PREF_KEY_INITIAL_ALARM_LIST, prefTimeRandom);
    if (!areAlarmsEnabled() || prefTimeNever.equals(settingsHours)) {
      Log.d(TAG, "Initial alarm disabled");
      alarmManager.cancel(initialAlarmPendingIntent);
      return;
    }
    int hoursToAdd;
    if (context.getResources().getString(R.string.pref_time_random).equals(settingsHours)) {
      // Random hour between 6 and 12.
      hoursToAdd = (int) (Math.random() * 7) + 6;
    } else {
      hoursToAdd = Integer.parseInt(settingsHours);
    }
    long alarmTime = getAlarmTime(hoursToAdd);
    Log.d(TAG, "Set initial alarm to " + new Date(alarmTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, initialAlarmPendingIntent);
  }

  /**
   * Is fired every day between 18 and 24.
   */
  public void setFinalAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeRandom = context.getResources().getString(R.string.pref_time_random);
    String settingsHours = sharedPreferences.getString(
        PreferencesService.PREF_KEY_FINAL_ALARM_LIST, prefTimeRandom);
    if (!areAlarmsEnabled() || prefTimeNever.equals(settingsHours)) {
      Log.d(TAG, "Final alarm disabled");
      alarmManager.cancel(finalAlarmPendingIntent);
      return;
    }
    int hoursToAdd;
    if (context.getResources().getString(R.string.pref_time_random).equals(settingsHours)) {
      // Random hour between 18 and 24.
      hoursToAdd = (int) (Math.random() * 7) + 18;
    } else {
      hoursToAdd = Integer.parseInt(settingsHours);
    }
    long alarmTime = getAlarmTime(hoursToAdd);
    Log.d(TAG, "Set final alarm to " + new Date(alarmTime));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, finalAlarmPendingIntent);
  }

  /**
   * Is fired every {@code dailyAlarmHours} during the day while challenge is started.
   */
  public void setDailyAlarm() {
    String prefTimeNever = context.getResources().getString(R.string.pref_time_never);
    String prefTimeEveryHour = "1";
    String settingsHours = sharedPreferences.getString(
        PreferencesService.PREF_KEY_DAILY_ALARM_LIST, prefTimeEveryHour);
    if (!areAlarmsEnabled() || prefTimeNever.equals(settingsHours)) {
      Log.d(TAG, "Daily alarm disabled");
      stopDailyAlarm();
      return;
    }
    long hoursToAdd;
    if (Utils.getInstance().isDebug()) {
      hoursToAdd = Utils.getInstance().getDebugAlarmRepeatTime();
    } else {
      hoursToAdd = AlarmManager.INTERVAL_HOUR * Integer.parseInt(settingsHours);
    }
    long alarmStartTime = new Date().getTime();
    Log.d(TAG, "Set daily alarm to " + new Date(alarmStartTime + hoursToAdd));
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime + hoursToAdd,
        dailyAlarmPendingIntent);
  }

  public void stopDailyAlarm() {
    alarmManager.cancel(dailyAlarmPendingIntent);
  }
}
