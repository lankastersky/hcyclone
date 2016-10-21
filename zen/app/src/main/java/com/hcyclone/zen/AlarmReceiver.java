package com.hcyclone.zen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

  private static final String TAG = AlarmReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    int alarmId = intent.getIntExtra(AlarmService.PARAM_ID, 0);
    Log.d(TAG, "Alarm received with id: " + alarmId);
    switch (alarmId) {
      case AlarmService.ALARM_CODE_SERVICE:
        context.startService(new Intent(context, FirebaseService.class));
        AlarmService.getInstance().updateServiceAlarm();
        break;
      case AlarmService.ALARM_CODE_INITIAL:
        NotificationManager.getInstance().showInitialAlarmNotification();
        AlarmService.getInstance().updateInitialAlarm();
        break;
      case AlarmService.ALARM_CODE_FINAL:
        NotificationManager.getInstance().showFinalAlarmNotification();
        AlarmService.getInstance().updateFinalAlarm();
        AlarmService.getInstance().stopReminderAlarm();
        break;
      case AlarmService.ALARM_CODE_REMINDER:
        NotificationManager.getInstance().showReminderAlarmNotification();
        break;
    }
  }

}
