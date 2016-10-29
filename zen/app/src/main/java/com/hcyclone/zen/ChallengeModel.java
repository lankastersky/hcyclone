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

  //public static final Map<String, Challenge> ITEM_MAP = new HashMap<>();
  //private static final int COUNT = 25;
  private static final String KEY_CHALLENGE_STATUSES = "challenge_statuses";
  private static final String KEY_CURRENT_CHALLENGE_SHOWN_TIME = "current_challenge_shown_time";
  private static final String CURRENT_CHALLENGE_ID_KEY = "current_challenge_id";
  private static final String CURRENT_CHALLENGE_KEY = "current_challenge";

  private String currentChallengeId;
  private long currentChallengeShownTime;
  private final Map<String, Challenge> challengeMap = new HashMap<>();
  private Context context;
  private Gson gson;

//  static {
//    // Add some sample items.
//    for (int i = 1; i <= COUNT; i++) {
//      addItem(createDummyItem(i));
//    }
//  }

  private ChallengeModel() {
  }

  public static ChallengeModel getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
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
      challengeMap.put(challenge.id, challenge);
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
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String currentChallengeString = sharedPreferences.getString(CURRENT_CHALLENGE_KEY, null);
    return gson.fromJson(currentChallengeString, Challenge.class);
  }

  private void updateCurrentChallenge() {
    Log.d(TAG, "updateCurrentChallenge");
    getCurrentChallenge().updateStatus();
    storeChallengeStatuses();
    storeCurrentChallenge();
  }

  private void selectCurrentChallengeIfNeeded() {
    if (TextUtils.isEmpty(currentChallengeId)) {
      currentChallengeId = getNewChallengeId();
    } else {
      if (isTimeToDecline()) {
        getChallengesMap().get(currentChallengeId).decline();
      }
      if (isNewChallengeRequired()) {
        currentChallengeId = getNewChallengeId();
        Challenge challenge = getChallengesMap().get(currentChallengeId);
        if (challenge.getStatus() != Challenge.UNKNOWN) {
          // Challenge was shown before. Reset the status
          challenge.setStatus(Challenge.UNKNOWN);
        }
      }
    }
    storeChallengeStatuses();
    storeCurrentChallenge();
    Log.d(TAG, "Current challenge id:" + currentChallengeId);
  }

  // Challenge expires at midnight of next day.
  private boolean isChallengeTimeExpired() {
    Date timeToDecline = Utils.getMidnight(currentChallengeShownTime);
    return timeToDecline.before(new Date());
  }

  private boolean isTimeToDecline() {
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.SHOWN
        || challenge.getStatus() == Challenge.ACCEPTED) {
      return isChallengeTimeExpired();
    }
    return false;
  }

  private boolean isNewChallengeRequired() {
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.FINISHED
        || challenge.getStatus() == Challenge.DECLINED) {
      return isChallengeTimeExpired();
    }
    return false;
  }

  @NonNull
  private String getNewChallengeId() {
    String challengeId;
    List<Challenge> nonacceptedChallenges = getChallengesMap(Challenge.UNKNOWN);
    if (nonacceptedChallenges.size() > 0) {
      Challenge challenge = getRandomChallenge(nonacceptedChallenges);
      challengeId = challenge.id;
    } else {
      List<Challenge> declinedChallenges = getChallengesMap(Challenge.DECLINED);
      if (nonacceptedChallenges.size() > 0) {
        Challenge challenge = getRandomChallenge(declinedChallenges);
        challengeId = challenge.id;
      } else {
        // All challenges are finished. Return random old one.
        Challenge challenge = getRandomChallenge(getChallengesMap().values());
        challengeId = challenge.id;
      }
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
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    sharedPreferences.edit().putLong(KEY_CURRENT_CHALLENGE_SHOWN_TIME, currentChallengeShownTime)
        .apply();
    sharedPreferences.edit().putString(CURRENT_CHALLENGE_ID_KEY, currentChallengeId).apply();

    String currentChallengeString = gson.toJson(getCurrentChallenge());
    sharedPreferences.edit().putString(CURRENT_CHALLENGE_KEY, currentChallengeString).apply();
  }

  private void restoreCurrentChallenge() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    sharedPreferences.edit().putString(KEY_CHALLENGE_STATUSES, statusesString).apply();
  }

  private void restoreChallengeStatuses(Map<String, Challenge> challengeMap) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

//  private static void addItem(Challenge item) {
//    ITEM_MAP.put(item.id, item);
//  }
//
//  private static Challenge createDummyItem(int position) {
//    return new Challenge(String.valueOf(position), "Challenge " + position, makeDetails(position));
//  }
//
//  private static String makeDetails(int position) {
//    StringBuilder builder = new StringBuilder();
//    builder.append("Details about Challenge: ").append(position);
//    for (int i = 0; i < position; i++) {
//      builder.append("\nMore details information here.");
//    }
//    return builder.toString();
//  }

}
