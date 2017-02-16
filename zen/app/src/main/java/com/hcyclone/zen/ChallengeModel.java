package com.hcyclone.zen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Date;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class ChallengeModel {

  private static final String TAG = ChallengeModel.class.getSimpleName();
  private static final ChallengeModel instance = new ChallengeModel();

  private static final String SHARED_PREFERENCES_NAME = "com.hcyclone.zen.ChallengeModel";

  private static final String KEY_CHALLENGE_STATUSES = "challenge_statuses";
  private static final String KEY_CURRENT_CHALLENGE_SHOWN_TIME = "current_challenge_shown_time";
  private static final String CURRENT_CHALLENGE_ID_KEY = "current_challenge_id";
  private static final String CURRENT_CHALLENGE_KEY = "current_challenge";
  private SharedPreferences sharedPreferences;

  private String currentChallengeId;
  private long currentChallengeShownTime;
  // Map <challenge id, challenge>
  private final Map<String, Challenge> challengeMap = new HashMap<>();
  private Context context;
  private Gson gson;

  private ChallengeModel() {}

  public static ChallengeModel getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    gson = new Gson();
  }

  public Challenge getCurrentChallenge() {
    return getChallengesMap().get(currentChallengeId);
  }

  public List<Challenge> getFinishedChallenges() {
    List<Challenge> challenges = getChallengesMap(Challenge.FINISHED);
    // Sort by finished time in reverse order.
    Collections.sort(challenges, new Comparator<Challenge>() {
      @Override
      public int compare(Challenge challenge1, Challenge challenge2) {
        return -Long.valueOf(challenge1.getFinishedTime()).compareTo(challenge2.getFinishedTime());
      }
    });
    return challenges;
  }

  public Challenge getChallenge(String challengeId) {
    return challengeMap.get(challengeId);
  }

  private Map<String, Challenge> getChallengesMap() {
    return challengeMap;
  }

  public void loadChallenges(List<Challenge> challenges) {
    for (Challenge challenge : challenges) {
      challengeMap.put(challenge.getId(), challenge);
    }
    restoreChallengeStatuses(challengeMap);
    restoreCurrentChallenge();
    selectCurrentChallengeIfNeeded();
  }

  public void setCurrentChallengeShown() {
    if (getCurrentChallenge().getStatus() != Challenge.UNKNOWN) {
      return;
    }
    currentChallengeShownTime = new Date().getTime();
    updateCurrentChallenge();
  }

  public void setCurrentChallengeAccepted() {
    updateCurrentChallenge();
  }

  public void setCurrentChallengeFinished() {
    updateCurrentChallenge();
  }

  public Challenge getSerializedCurrentChallenge() {
    String currentChallengeString = sharedPreferences.getString(CURRENT_CHALLENGE_KEY, null);
    return gson.fromJson(currentChallengeString, Challenge.class);
  }

  private void updateCurrentChallenge() {
    Log.d(TAG, "updateCurrentChallenge");
    getCurrentChallenge().updateStatus();
    storeChallengeStatuses();
    storeCurrentChallenge();
  }

  public boolean isTimeToFinishCurrentChallenge() {
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.ACCEPTED) {
      Date timeToFinish = Utils.get6PM(currentChallengeShownTime);
      return timeToFinish.before(new Date());
    }
    return false;
  }

  private void selectCurrentChallengeIfNeeded() {
    if (TextUtils.isEmpty(currentChallengeId)) {
      currentChallengeId = getNewChallengeId();
    } else {
      boolean newChallengeRequired = false;
      Challenge challenge = getChallengesMap().get(currentChallengeId);
      switch (challenge.getStatus()) {
        case Challenge.UNKNOWN:
          break;
        case Challenge.SHOWN:
          if (isCurrentChallengeTimeExpired()) {
            challenge.decline();
            newChallengeRequired = true;
          }
          break;
        case Challenge.ACCEPTED:
          if (isCurrentChallengeTimeExpired()) {
            challenge.reset();
            newChallengeRequired = true;
          }
          break;
        case Challenge.FINISHED:
        case Challenge.DECLINED:
          if (isCurrentChallengeTimeExpired()) {
            newChallengeRequired = true;
          }
          break;
        default:
          Log.e(TAG, "Wrong status of current challenge: " + currentChallengeId);
      }
      if (newChallengeRequired) {
        currentChallengeId = getNewChallengeId();
      }
    }
    storeChallengeStatuses();
    storeCurrentChallenge();
    Log.d(TAG, "Current challenge id:" + currentChallengeId);
  }

  // Challenge expires at midnight of next day.
  private boolean isCurrentChallengeTimeExpired() {
    Date timeToDecline = Utils.getMidnight(currentChallengeShownTime);
    return timeToDecline.before(new Date());
  }

  /**
   * If there are nonaccepted challenges, get random challenge from them taking into account levels.
   * Else if there are declined challenges, get random challenge from them.
   * Else get random challenge from all and is not equal to previous one.
   */
  @NonNull
  private String getNewChallengeId() {
    String challengeId;
    Challenge challenge;
    List<Challenge> nonacceptedChallenges = getChallengesMap(Challenge.UNKNOWN);
    nonacceptedChallenges = filterNonacceptedChallengesByLevel(nonacceptedChallenges);
    if (nonacceptedChallenges.size() > 0) {
      challenge = getRandomChallenge(nonacceptedChallenges);
      challengeId = challenge.getId();
    } else {
      List<Challenge> declinedChallenges = getChallengesMap(Challenge.DECLINED);
      if (declinedChallenges.size() > 0) {
        challenge = getRandomChallenge(declinedChallenges);
        challengeId = challenge.getId();
      } else {
        // All challenges are finished. Return random old one.
        challenge = getRandomChallenge(getChallengesMap().values());
        if (getCurrentChallenge() != null) {
          while (challenge.getId().equals(getCurrentChallenge().getId())) {
            challenge = getRandomChallenge(getChallengesMap().values());
          }
        }
        challengeId = challenge.getId();
      }
      challenge.reset();
    }
    return challengeId;
  }

  @NonNull
  private Challenge getRandomChallenge(@NonNull Collection<Challenge> challenges) {
    int id = (int) (Math.random() * challenges.size());
    int i = 0;
    Challenge result = challenges.iterator().next();
    for (Challenge challenge : challenges) {
      if (i++ == id) {
        result = challenge;
        break;
      }
    }
    return result;
  }

  /**
   * Low-level challenges are available from the beginning.
   * Medium-level challenges are available when 1/3 of challenges is accepted.
   * High-level challenges are available when 2/3 of challenges is accepted.
   */
  private List<Challenge> filterNonacceptedChallengesByLevel(
      @NonNull Collection<Challenge> nonaccepted) {

    Collection<Challenge> challenges = getChallengesMap().values();
    double acceptedProportion = (challenges.size() - nonaccepted.size()) / challenges.size();
    boolean acceptMedium = acceptedProportion >= 1D / 3;
    boolean acceptHigh = acceptedProportion >= 2D / 3;

    List<Challenge> filteredChallenges = new ArrayList<>();
    for (Challenge challenge : nonaccepted) {
      switch (challenge.getLevel()) {
        case Challenge.LEVEL_LOW:
          filteredChallenges.add(challenge);
          break;
        case Challenge.LEVEL_MEDIUM:
          if (acceptMedium) {
            filteredChallenges.add(challenge);
          }
          break;
        case Challenge.LEVEL_HIGH:
          if (acceptHigh) {
            filteredChallenges.add(challenge);
          }
      }
    }
    return filteredChallenges;
  }

  @NonNull
  private List<Challenge> getChallengesMap(int status) {
    List<Challenge> challenges = new ArrayList<>();
    for (Challenge challenge : getChallengesMap().values()) {
      if (challenge.getStatus() == status) {
        challenges.add(challenge);
      }
    }
    return challenges;
  }

  private void storeCurrentChallenge() {
    sharedPreferences.edit().putLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, currentChallengeShownTime)
        .apply();
    sharedPreferences.edit().putString(CURRENT_CHALLENGE_ID_KEY, currentChallengeId).apply();

    String currentChallengeString = gson.toJson(getCurrentChallenge());
    sharedPreferences.edit().putString(CURRENT_CHALLENGE_KEY, currentChallengeString).apply();
  }

  private void restoreCurrentChallenge() {
    currentChallengeShownTime = sharedPreferences.getLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, 0);
    currentChallengeId = sharedPreferences.getString(CURRENT_CHALLENGE_ID_KEY, null);
  }

  private void storeChallengeStatuses() {
    List<ChallengeStatus> statuses = new ArrayList<>();
    for (Challenge challenge : challengeMap.values()) {
      statuses.add(new ChallengeStatus(challenge.getId(), challenge.getStatus(),
          challenge.getFinishedTime()));
    }
    String statusesString = gson.toJson(statuses);
    sharedPreferences.edit().putString(KEY_CHALLENGE_STATUSES, statusesString).apply();
  }

  private void restoreChallengeStatuses(Map<String, Challenge> challengeMap) {
    String statusesString = sharedPreferences.getString(KEY_CHALLENGE_STATUSES, null);
    if (!TextUtils.isEmpty(statusesString)) {
      List<ChallengeStatus> statuses = gson.fromJson(statusesString,
          new TypeToken<List<ChallengeStatus>>(){}.getType());
      for (ChallengeStatus challengeStatus : statuses) {
        Challenge challenge = challengeMap.get(challengeStatus.id);
        challenge.setStatus(challengeStatus.status);
        challenge.setFinishedTime(challengeStatus.finishedTime);
      }
    }
  }
}
