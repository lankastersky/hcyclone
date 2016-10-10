package com.hcyclone.zen;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;

import android.util.Log;

import java.util.Calendar;

/**
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
 ?
 */
public final class AlarmService {

  private static final AlarmService instance = new AlarmService();
  private Context context;
  private PendingIntent alarmSender;

  private AlarmService() {
  }

  public static AlarmService getInstance() {
    return instance;
  }

  public void init(Context context) {
    this.context = context;
    alarmSender = PendingIntent.getBroadcast(context,
        0, new Intent(context, AlarmReceiver.class), 0);
  }

  public void startAlarm() {
    Log.d(AlarmService.class.getSimpleName(), "Set alarm");
    Calendar c = Calendar.getInstance();
    // TODO: set time properly.
    c.add(Calendar.SECOND, 10);
    long firstTime = c.getTimeInMillis();
    // Schedule the alarm!
    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC_WAKEUP, firstTime, alarmSender);
  }
}
