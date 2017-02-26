package com.hcyclone.zen;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Archives and unarchives challenges data.
 */
public class ChallengeArchiver {

  private static final String TAG = ChallengeArchiver.class.getSimpleName();

  private static final String SHARED_PREFERENCES_NAME = "com.hcyclone.zen.ChallengeModel";

  private static final String KEY_CHALLENGE_DATA = "challenge_data";
  private static final String KEY_CHALLENGES = "challenges";
  private static final String KEY_CURRENT_CHALLENGE_SHOWN_TIME = "current_challenge_shown_time";
  private static final String KEY_CURRENT_CHALLENGE = "current_challenge";
  private static final String KEY_CURRENT_LEVEL = "current_challenge_level";

  private Gson gson;
  private SharedPreferences sharedPreferences;

  public ChallengeArchiver(@NonNull Context context) {
    sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    gson = new Gson();
  }

  public Challenge restoreCurrentChallenge() {
    String currentChallengeString = sharedPreferences.getString(KEY_CURRENT_CHALLENGE, null);
    return gson.fromJson(currentChallengeString, Challenge.class);
  }

  public void storeCurrentChallenge(Challenge challenge) {
    if (challenge == null) {
      Log.e(TAG, "Failed to store current challenge because it's null");
      return;
    }
    String currentChallengeString = gson.toJson(challenge);
    sharedPreferences.edit().putString(KEY_CURRENT_CHALLENGE, currentChallengeString).apply();
  }

  public void storeCurrentChallengeShownTime(long shownTime) {
    sharedPreferences.edit().putLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, shownTime)
        .apply();
  }

  public long restoreCurrentChallengeShownTime() {
    return sharedPreferences.getLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, 0);
  }

  public void storeLevel(int level) {
    sharedPreferences.edit().putInt(KEY_CURRENT_LEVEL, level).apply();
  }

  public int restoreLevel() {
    return sharedPreferences.getInt(KEY_CURRENT_LEVEL, Challenge.LEVEL_LOW);
  }

  public void storeChallengeData(Map<String, Challenge> challengeMap) {
    List<ChallengeData> data = new ArrayList<>();
    for (Challenge challenge : challengeMap.values()) {
      data.add(new ChallengeData(challenge.getId(), challenge.getStatus(),
          challenge.getFinishedTime(), challenge.getRating()));
    }
    String dataString = gson.toJson(data);
    sharedPreferences.edit().putString(KEY_CHALLENGE_DATA, dataString).apply();
  }

  public void restoreChallengeData(Map<String, Challenge> challengeMap) {
    String dataString = sharedPreferences.getString(KEY_CHALLENGE_DATA, null);

    if (!TextUtils.isEmpty(dataString)) {
      List<ChallengeData> data = gson.fromJson(dataString,
          new TypeToken<List<ChallengeData>>(){}.getType());

      for (ChallengeData challengeData : data) {
        Challenge challenge = challengeMap.get(challengeData.id);
        challenge.setStatus(challengeData.status);
        challenge.setFinishedTime(challengeData.finishedTime);
        challenge.setRating(challengeData.rating);
      }
    }
  }

  public void storeChallenges(List<Challenge> challenges) {
    String dataString = gson.toJson(challenges);
    sharedPreferences.edit().putString(KEY_CHALLENGES, dataString).apply();
  }

  public List<Challenge> restoreChallenges() {
    List<Challenge> challenges = new ArrayList<>();
    String dataString = sharedPreferences.getString(KEY_CHALLENGES, null);

    if (!TextUtils.isEmpty(dataString)) {
      challenges = gson.fromJson(dataString,
          new TypeToken<List<Challenge>>(){}.getType());
    }
    return challenges;
  }
}
