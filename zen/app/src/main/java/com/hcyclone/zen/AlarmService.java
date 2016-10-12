package com.hcyclone.zen;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;

import android.util.Log;

import java.util.Calendar;

/*
start app (1st time)
    create daily repeatable alarm with firetime (at night) if not yet?
        decline current challenge if needed
        go to server and gets challenges
        ask model for next challenge
        create initial alarm?

When fired daily alarm
    decline current challenge if needed
    go to server and gets challenges
    ask model for next challenge
    create initial alarm

create initial alarm:
    generates accept time, stores it
    create initial alarm for this challenge

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

  public static final String ID_PARAM = "id";
  public static final int NIGHTLY_ALARM_CODE = 1;

  private static final AlarmService instance = new AlarmService();
  private Context context;
  private PendingIntent nightlyAlarmPengingIntent;

  private AlarmService() {
  }

  public static AlarmService getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;
    Intent nightlyAlarmIntent = new Intent(context, AlarmReceiver.class);
    nightlyAlarmIntent.putExtra(ID_PARAM, NIGHTLY_ALARM_CODE);
    nightlyAlarmPengingIntent = PendingIntent.getBroadcast(context, NIGHTLY_ALARM_CODE,
        nightlyAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  /**
   * Nightly alarm is fired at 2am every night.
   */
  public void createNightlyAlarmIfNeeded() {
    boolean alarmUp = (PendingIntent.getBroadcast(context, NIGHTLY_ALARM_CODE,
        new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
    if (alarmUp) {
      Log.d(TAG, "Nightly alarm is already active");
    }

    Log.d(TAG, "Set nightly alarm");

    Calendar calendar = Calendar.getInstance();
    long alarmTime;
    if (BuildConfig.DEBUG) {
      alarmTime = 5_000;
    } else {
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, 2);
      alarmTime = calendar.getTimeInMillis();
    }

    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
        AlarmManager.INTERVAL_DAY, nightlyAlarmPengingIntent);
  }
}
