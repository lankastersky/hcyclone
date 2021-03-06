package com.hcyclone.zen;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Archives and unarchives challenges data. */
public class ChallengeArchiver {

  static final String SHARED_PREFERENCES_NAME = "com.hcyclone.zen.model.ChallengeModel";

  private static final String TAG = ChallengeArchiver.class.getSimpleName();
  private static final String KEY_CHALLENGE_DATA = "challenge_data";
  private static final String KEY_CHALLENGES = "challenges";
  private static final String KEY_CURRENT_CHALLENGE_SHOWN_TIME = "current_challenge_shown_time";
  private static final String KEY_CURRENT_CHALLENGE = "current_challenge";
  private static final String KEY_CURRENT_LEVEL = "current_challenge_level";

  private static final Gson GSON = new Gson();
  private static final Type CHALLENGE_DATA_LIST_TYPE =
      new TypeToken<List<ChallengeData>>() {
      }.getType();
  private static final Type CHALLENGE_LIST_TYPE = new TypeToken<List<Challenge>>() {
  }.getType();

  private final SharedPreferences sharedPreferences;
  private final AppLifecycleManager appLifecycleManager;

  public ChallengeArchiver(Context context, AppLifecycleManager appLifecycleManager) {
    this.appLifecycleManager = appLifecycleManager;
    sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  public Challenge restoreCurrentChallenge() {
    String currentChallengeString = sharedPreferences.getString(KEY_CURRENT_CHALLENGE, null);
    return GSON.fromJson(currentChallengeString, Challenge.class);
  }

  public void storeCurrentChallenge(Challenge challenge) {
    if (challenge == null) {
      Log.e(TAG, "Failed to store current challenge because it's null");
      return;
    }
    String currentChallengeString = GSON.toJson(challenge);
    sharedPreferences.edit().putString(KEY_CURRENT_CHALLENGE, currentChallengeString).apply();
    requestBackup();
  }

  public void storeCurrentChallengeShownTime(long shownTime) {
    sharedPreferences.edit().putLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, shownTime)
        .apply();
    requestBackup();
  }

  public long restoreCurrentChallengeShownTime() {
    return sharedPreferences.getLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, 0);
  }

  public void storeLevel(@Challenge.LevelType int level) {
    sharedPreferences.edit().putInt(KEY_CURRENT_LEVEL, level).apply();
    requestBackup();
  }

  public int restoreLevel() {
    return sharedPreferences.getInt(KEY_CURRENT_LEVEL, Challenge.LEVEL_LOW);
  }

  public void storeChallengeData(Map<String, Challenge> challengeMap) {
    List<ChallengeData> data = new ArrayList<>();
    for (Challenge challenge : challengeMap.values()) {
      data.add(new ChallengeData(
          challenge.getId(),
          challenge.getStatus(),
          challenge.getFinishedTime(),
          challenge.getRating(),
          challenge.getComments(),
          challenge.getPrevStatuses(),
          challenge.getPrevFinishedTimes(),
          challenge.getPrevRatings()
      ));
    }
    String dataString = GSON.toJson(data);
    sharedPreferences.edit().putString(KEY_CHALLENGE_DATA, dataString).apply();
    requestBackup();
  }

  public void restoreChallengeData(Map<String, Challenge> challengeMap) {
    String dataString = sharedPreferences.getString(KEY_CHALLENGE_DATA, null);

    if (!TextUtils.isEmpty(dataString)) {
      List<ChallengeData> data = GSON.fromJson(dataString, CHALLENGE_DATA_LIST_TYPE);

      for (ChallengeData challengeData : data) {
        Challenge challenge = challengeMap.get(challengeData.id);
        if (challenge == null) {
          Log.e(TAG, "Failed to find challenge " + challengeData.id);
          continue;
        }
        challenge.setStatus(challengeData.status);
        challenge.setFinishedTime(challengeData.finishedTime);
        challenge.setRating(challengeData.rating);
        challenge.setComments(challengeData.comments);
        challenge.setPrevStatuses(challengeData.prevStatuses);
        challenge.setPrevFinishedTimes(challengeData.prevFinishedTimes);
        challenge.setPrevRatings(challengeData.prevRatings);
      }
    }
  }

  public void storeChallenges(List<Challenge> challenges) {
    String dataString = GSON.toJson(challenges);
    sharedPreferences.edit().putString(KEY_CHALLENGES, dataString).apply();
    requestBackup();
  }

  public List<Challenge> restoreChallenges() {
    List<Challenge> challenges = new ArrayList<>();
    String dataString = sharedPreferences.getString(KEY_CHALLENGES, null);

    if (!TextUtils.isEmpty(dataString)) {
      challenges = GSON.fromJson(dataString, CHALLENGE_LIST_TYPE);
    }
    return challenges;
  }

  private void requestBackup() {
    appLifecycleManager.requestBackup();
  }
}
