package com.hcyclone.zen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hcyclone.zen.Log;

public final class AlarmReceiver extends BroadcastReceiver {

  private static final String TAG = AlarmReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    int alarmId = intent.getIntExtra(AlarmService.PARAM_ID, 0);
    Log.d(TAG, "Alarm received with id: " + alarmId);
    switch (alarmId) {
      case AlarmService.ALARM_CODE_SERVICE:
        new ChallengesLoader(null, context).loadChallenges(context);
        // Restart alarm to make it random.
        AlarmService.getInstance().setServiceAlarm();
        break;
      case AlarmService.ALARM_CODE_INITIAL:
        NotificationService.getInstance().showInitialAlarmNotification();
        // Restart alarm to make it random.
        AlarmService.getInstance().setInitialAlarm();
        break;
      case AlarmService.ALARM_CODE_FINAL:
        NotificationService.getInstance().showFinalAlarmNotification();
        // Restart alarm to make it random.
        AlarmService.getInstance().setFinalAlarm();
        AlarmService.getInstance().stopDailyAlarm();
        break;
      case AlarmService.ALARM_CODE_DAILY:
        NotificationService.getInstance().showDailyAlarmNotification();
        break;
    }
  }

}
