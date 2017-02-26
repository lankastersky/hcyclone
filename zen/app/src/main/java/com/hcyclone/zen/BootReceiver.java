package com.hcyclone.zen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      Log.d(BootReceiver.class.getSimpleName(), "Setting alarms on boot");

      AlarmService.getInstance().init(context);
      AlarmService.getInstance().setAlarms();

      Challenge challenge = ChallengeModel.getInstance().getSerializedCurrentChallenge();
      if (challenge != null && challenge.getStatus() == Challenge.ACCEPTED) {
        AlarmService.getInstance().setDailyAlarm();
      }
    }
  }
}
