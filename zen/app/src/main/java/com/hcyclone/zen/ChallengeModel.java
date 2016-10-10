package com.hcyclone.zen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

public final class ChallengeModel {

  private static final String TAG = ChallengeModel.class.getSimpleName();
  private static final ChallengeModel instance = new ChallengeModel();

  //public static final Map<String, Challenge> ITEM_MAP = new HashMap<>();
  //private static final int COUNT = 25;
  private static final String CHALLENGE_STATUSES_KEY = "challenge_statuses";
  private static final String CURRENT_CHALLENGE_SHOWN_TIME_KEY = "current_challenge_shown_time";
  private static final String CURRENT_CHALLENGE_ID_KEY = "current_challenge_id";

  private String currentChallengeId;
  private long currentChallengeShownTime;
  private Map<String, Challenge> challengeMap = new HashMap<>();
  private Context context;

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

  public void init(Context context) {
    this.context = context;
  }

  public Challenge getCurrentChallenge() {
    //updateCurrentChallengeId();
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

  public Map<String, Challenge> getChallengesMap() {
    return challengeMap; //ITEM_MAP;
  }

  public void loadChallenges(final FirebaseAdapter.FirebaseDataListener listener) {
    challengeMap.clear();
    FirebaseAdapter.getInstance().getChallenges(new FirebaseAdapter.FirebaseDataListener() {
      @Override
      public void onError(Exception e) {
        listener.onError(e);
      }

      @Override
      public void onChallenges(List<Challenge> challenges) {
        for (Challenge challenge : challenges) {
          challengeMap.put(challenge.id, challenge);
        }
        restoreChallengeStatuses(challengeMap);
        restoreCurrentChallenge();
        updateCurrentChallengeId();
        listener.onChallenges(challenges);
      }
    });
  }

  public void updateCurrentChallenge() {
    getCurrentChallenge().updateStatus();
    if (getCurrentChallenge().getStatus() == Challenge.SHOWN) {
      currentChallengeShownTime = new Date().getTime();
    }
    storeChallengeStatuses();
    storeCurrentChallenge();
  }

  public void updateCurrentChallengeId() {
    if (TextUtils.isEmpty(currentChallengeId)) {
      currentChallengeId = getNewChallengeId();
    } else {
      if (isTimeToDecline()) {
        getChallengesMap().get(currentChallengeId).decline();
      }
      if (isNewChallengeRequired()) {
        currentChallengeId = getNewChallengeId();
      }
    }
    storeChallengeStatuses();
    storeCurrentChallenge();
  }

  private boolean isChallengeTimeExpired() {
    Date timeToDecline = new Date(currentChallengeShownTime);
    // today
    Calendar date = new GregorianCalendar();
    Date timeNow = date.getTime();
    date.setTime(timeToDecline);
    // reset hour, minutes, seconds and millis
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    if (BuildConfig.DEBUG) {
      date.add(Calendar.MINUTE, 5);
    } else {
      // midnight of next day
      date.add(Calendar.DAY_OF_MONTH, 1);
    }
    if (date.getTime().after(timeNow)) {
      return true;
    }
    return false;
  }

  // Challenge is declined after midnight.
  private boolean isTimeToDecline() {
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.SHOWN
        || challenge.getStatus() == Challenge.ACCEPTED) {
      return isChallengeTimeExpired();
    }
    return false;
  }

  // New challenge is available after midnight.
  private boolean isNewChallengeRequired() {
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.FINISHED
        || challenge.getStatus() == Challenge.DECLINED) {
      return isChallengeTimeExpired();
    }
    return false;
  }

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

  private Challenge getRandomChallenge(Collection<Challenge> challenges) {
    int id = (int) (Math.random() * challenges.size());
    int i = 0;
    for (Challenge challenge : challenges) {
      if (i++ == id) {
        return challenge;
      }
    }
    return null;
  }

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
    sharedPreferences.edit().putLong(CURRENT_CHALLENGE_SHOWN_TIME_KEY, currentChallengeShownTime)
        .apply();
    sharedPreferences.edit().putString(CURRENT_CHALLENGE_ID_KEY, currentChallengeId).apply();
  }

  private void restoreCurrentChallenge() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    currentChallengeShownTime = sharedPreferences.getLong(CURRENT_CHALLENGE_SHOWN_TIME_KEY, 0);
    currentChallengeId = sharedPreferences.getString(CURRENT_CHALLENGE_ID_KEY, null);
  }

  private void storeChallengeStatuses() {
    Map<String, Integer> statusesMap = new HashMap<>();
    for (Challenge challenge : challengeMap.values()) {
      statusesMap.put(challenge.id, challenge.getStatus());
    }
    String statusesString = statusesMap.toString();
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    sharedPreferences.edit().putString(CHALLENGE_STATUSES_KEY, statusesString).apply();
  }

  private void restoreChallengeStatuses(Map<String, Challenge> challengeMap) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String statusesString = sharedPreferences.getString(CHALLENGE_STATUSES_KEY, null);
    if (!TextUtils.isEmpty(statusesString)) {
      Map<String, String> statusesMap;
      try {
        statusesMap = Splitter
            .on(",")
            .trimResults(CharMatcher.anyOf("{} "))
            .withKeyValueSeparator("=")
            .split(statusesString);
      } catch (Exception e) {
        Log.e(TAG, "Service status parse error: " + e.toString());
        return;
      }
      for (String challengeId : statusesMap.keySet()) {
        Challenge challenge = challengeMap.get(challengeId);
        int status = Integer.parseInt(statusesMap.get(challengeId));
        challenge.setStatus(status);
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
