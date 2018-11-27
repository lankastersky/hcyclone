package com.hcyclone.zen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hcyclone.zen.App;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;

public final class BootReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      Log.i(BootReceiver.class.getSimpleName(), "Setting alarms on boot");

      AlarmService.getInstance().init(context);
      AlarmService.getInstance().setAlarms();

      ChallengeModel challengeModel = ((App) context.getApplicationContext()).getChallengeModel();
      Challenge challenge = challengeModel.getSerializedCurrentChallenge();
      if (challenge != null && challenge.getStatus() == Challenge.ACCEPTED) {
        AlarmService.getInstance().setDailyAlarm();
      }
    }
  }
}
