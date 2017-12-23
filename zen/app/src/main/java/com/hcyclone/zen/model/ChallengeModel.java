package com.hcyclone.zen.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hcyclone.zen.ChallengeArchiver;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChallengeModel {

  private static final Calendar CALENDAR = Calendar.getInstance();

  private static final String TAG = ChallengeModel.class.getSimpleName();
  private static final ChallengeModel instance = new ChallengeModel();

  private final Map<String, Challenge> challengeMap = new HashMap<>();

  private String currentChallengeId;
  private long currentChallengeShownTime;
  private int level;
  private ChallengeArchiver challengeArchiver;

  private ChallengeModel() {}

  public static ChallengeModel getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    challengeArchiver = new ChallengeArchiver(context);
  }

  public Challenge getCurrentChallenge() {
    return getChallengesMap().get(currentChallengeId);
  }

  public List<Challenge> getFinishedChallengesSortedDesc() {
    List<Challenge> challenges = getFinishedChallenges();
    // Sort by finished time in reverse order.
    Collections.sort(challenges, new Comparator<Challenge>() {
      @Override
      public int compare(Challenge challenge1, Challenge challenge2) {
        return -Long.valueOf(challenge1.getFinishedTime()).compareTo(challenge2.getFinishedTime());
      }
    });
    return challenges;
  }

  public List<Challenge> getFinishedChallengesSorted() {
    List<Challenge> challenges = getFinishedChallenges();
    // Sort by finished time.
    Collections.sort(challenges, new Comparator<Challenge>() {
      @Override
      public int compare(Challenge challenge1, Challenge challenge2) {
        return Long.valueOf(challenge1.getFinishedTime()).compareTo(challenge2.getFinishedTime());
      }
    });
    return challenges;
  }

  /**
   * Returns normalized (divided by {@link #getMaxRating(Context)}) average rating among finished
   * challenges.
   */
  public float getAverageRating(Context context) {
    float averageRating = 0;
    List<Challenge> finishedChallenges = getFinishedChallenges();
    for (Challenge challenge : finishedChallenges) {
      averageRating += challenge.getRating();
    }
    averageRating = averageRating / finishedChallenges.size() / getMaxRating(context);
    return averageRating;
  }

  public int getMaxRating(Context context) {
    return context.getResources().getInteger(R.integer.stars_amount);
  }

  public int getShownChallengesNumber() {
    int shown = getChallengesMap(Challenge.SHOWN).size();
    return shown + getFinishedChallenges().size();
  }

  public List<Challenge> getFinishedChallenges() {
    return getChallengesMap(Challenge.FINISHED);
  }

  public Challenge getChallenge(String challengeId) {
    return challengeMap.get(challengeId);
  }

  private Map<String, Challenge> getChallengesMap() {
    return challengeMap;
  }

  public void loadChallenges() {
    Log.d(TAG, "Load challenges");
    if (challengeMap.isEmpty()) {
      List<Challenge> challenges;
      //if (!Utils.isDebug()) {
        challenges = challengeArchiver.restoreChallenges();
//      } else {
//        challenges = generateChallenges();
//      }
      for (Challenge challenge : challenges) {
        challengeMap.put(challenge.getId(), challenge);
      }
    }
    restoreState();
    selectChallengeIfNeeded();
  }

  private List<Challenge> generateChallenges() {
    List<Challenge> challenges = new ArrayList<>();
//      int days = 1;
//      int days = 2;
//      int days = 13;
//      int days = 14;
//      int days = 14 * 7 - 1;
//      int days = 14 * 7;
//      int days = 14 * 30 - 1;
    int days = 14 * 30;
//    int days = 200;
    for (int i = 0; i < days; i++) {
      Challenge challenge = new Challenge(
          "id" + i,
          "content" + i,
          "details" + i,
          "type" + i,
          i % 3, // level
          "source" + i,
          "url" + i,
          "quote" + i
      );
      int status = Challenge.FINISHED;
//      do {
//        status = (int) (Math.random() * Challenge.STATUSES_LENGTH);
//      } while (status == Challenge.ACCEPTED);
      challenge.setStatus(status);
      //challenge.setRating((float) Math.random() * getMaxRating(context));
      challenge.setRating(i % 5);
      CALENDAR.setTime(new Date());
      CALENDAR.add(Calendar.DAY_OF_MONTH, (int) (Math.random() * days));
      //CALENDAR.add(Calendar.DAY_OF_YEAR, i + 1);
      challenge.setFinishedTime(CALENDAR.getTimeInMillis());
      challenges.add(challenge);
    }
    return challenges;
  }

  public void saveChallenges(List<Challenge> challenges) {
    Log.d(TAG, "Save challenges");
    challengeMap.clear();
    for (Challenge challenge : challenges) {
      challengeMap.put(challenge.getId(), challenge);
    }
    challengeArchiver.storeChallenges(challenges);
    restoreState();
    selectChallengeIfNeeded();
  }

  private void restoreState() {
    challengeArchiver.restoreChallengeData(challengeMap);
    Challenge challenge = challengeArchiver.restoreCurrentChallenge();
    if (challenge != null) {
      currentChallengeId = challenge.getId();
    }
    currentChallengeShownTime = challengeArchiver.restoreCurrentChallengeShownTime();
    level = challengeArchiver.restoreLevel();
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
    return challengeArchiver.restoreCurrentChallenge();
  }

  private void updateCurrentChallenge() {
    Log.d(TAG, "updateCurrentChallenge");
    getCurrentChallenge().updateStatus();
    challengeArchiver.storeChallengeData(challengeMap);
    challengeArchiver.storeCurrentChallenge(getCurrentChallenge());
    challengeArchiver.storeCurrentChallengeShownTime(currentChallengeShownTime);
    challengeArchiver.storeLevel(level);
  }

  public boolean isTimeToAcceptChallenge() {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(currentChallengeShownTime);
    return Utils.isTimeLess6pm(date);
  }

  /**
   * Challenge is ready to be finished today after 6pm.
   */
  public boolean isTimeToFinishChallenge() {
    boolean result = false;
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.ACCEPTED) {
      Date timeToFinish = Utils.get6PM(currentChallengeShownTime);
      Calendar date = Calendar.getInstance();
      date.setTimeInMillis(currentChallengeShownTime);
//      Date now = new Date();
      // If is taken < 6pm, wait for 6pm.
//      if (date.get(Calendar.HOUR_OF_DAY) < 18) {
        result = timeToFinish.before(new Date());
//      } else {
//        // Wait for 6pm of next day.
//        timeToFinish = Utils.getInstance().getNextDay(timeToFinish);
//        result = timeToFinish.before(now);
//      }
    }
    Log.d(TAG, "isTimeToFinishChallenge: " + result);
    return result;
  }

  public boolean checkLevelUp() {
    Challenge challenge = getCurrentChallenge();
    int currentLevel = challenge.getLevel();
    if (currentLevel > getLevel() && currentLevel >= Challenge.LEVEL_LOW) {
      setLevel(challenge.getLevel());
      challengeArchiver.storeLevel(getLevel());
      return true;
    }
    return false;
  }

  @Challenge.LevelType
  public int getLevel() {
    return level;
  }

  private void setLevel(int level) {
    this.level = level;
  }

  /** Challenge expires at midnight of this day. */
  private boolean isChallengeTimeExpired() {
    boolean result;
    Date timeToDecline = Utils.getNextMidnight(currentChallengeShownTime);
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(currentChallengeShownTime);
    Date now = new Date();
    // If is taken < 6pm, wait for midnight.
//    if (date.get(Calendar.HOUR_OF_DAY) < 18) {
      result = timeToDecline.before(now);
//    } else {
//      // Wait for the next midnight.
//      timeToDecline = Utils.getInstance().getNextDay(timeToDecline);
//      result = timeToDecline.before(now);
//    }
    Log.d(TAG, "isChallengeTimeExpired: " + result);
    return result;
  }

  private void selectChallengeIfNeeded() {
    if (TextUtils.isEmpty(currentChallengeId)) {
      currentChallengeId = getNewChallengeId();
    } else {
      boolean newChallengeRequired = false;
      if (!getChallengesMap().containsKey(currentChallengeId)) {
        Log.e(TAG, "Failed to select current challenge with id " + currentChallengeId);
        return;
      }
      Challenge challenge = getChallengesMap().get(currentChallengeId);
      @Challenge.StatusType int status = challenge.getStatus();
      switch (status) {
        case Challenge.UNKNOWN:
          break;
        case Challenge.SHOWN:
          if (isChallengeTimeExpired()) {
            challenge.decline();
            newChallengeRequired = true;
          }
          break;
        case Challenge.ACCEPTED:
          if (isChallengeTimeExpired()) {
            challenge.reset();
            newChallengeRequired = true;
          }
          break;
        case Challenge.FINISHED:
        case Challenge.DECLINED:
          if (isChallengeTimeExpired()) {
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
    Challenge currentChallenge = getChallengesMap().get(currentChallengeId);
    if (currentChallenge == null) {
      Log.e(TAG, "Failed to get current challenge with id " + currentChallengeId);
      return;
    } else {
      Log.d(TAG, "Current challenge id:" + currentChallengeId + ": "
          + currentChallenge.getContent());
    }
    challengeArchiver.storeChallengeData(challengeMap);
    challengeArchiver.storeCurrentChallenge(currentChallenge);
    challengeArchiver.storeCurrentChallengeShownTime(currentChallengeShownTime);
  }

  /**
   * If there are nonfinished challenges, get random challenge from them taking into account levels.
   * Else if there are declined challenges, get random challenge from them.
   * Else get random challenge from all and is not equal to previous one.
   */
  @NonNull
  private String getNewChallengeId() {
    String challengeId = "";
    Challenge challenge = null;
    List<Challenge> nonfinishedChallenges = getChallengesMap(Challenge.UNKNOWN);
    nonfinishedChallenges.addAll(getChallengesMap(Challenge.ACCEPTED));
    nonfinishedChallenges.addAll(getChallengesMap(Challenge.SHOWN));
    nonfinishedChallenges = filterNonfinishedChallengesByLevel(nonfinishedChallenges);
    if (nonfinishedChallenges.size() > 0) {
      challenge = getRandomChallenge(nonfinishedChallenges);
      challengeId = challenge.getId();
    } else {
      List<Challenge> declinedChallenges = getChallengesMap(Challenge.DECLINED);
      if (declinedChallenges.size() > 0) {
        challenge = getRandomChallenge(declinedChallenges);
        challengeId = challenge.getId();
      } else if (!getChallengesMap().isEmpty()) {
        // All challenges are finished. Return random old one.
        challenge = getRandomChallenge(getChallengesMap().values());
        if (getCurrentChallenge() != null) {
          while (challenge.getId().equals(getCurrentChallenge().getId())) {
            challenge = getRandomChallenge(getChallengesMap().values());
          }
        }
        challengeId = challenge.getId();
      }
      if (challenge != null) {
        challenge.reset();
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

  /**
   * Low-level challenges are available from the beginning.
   * Medium-level challenges are available when 1/3 of challenges are finished.
   * High-level challenges are available when 2/3 of challenges are finished.
   */
  private List<Challenge> filterNonfinishedChallengesByLevel(
      @NonNull Collection<Challenge> nonfinished) {

    Collection<Challenge> challenges = getChallengesMap().values();
    if (challenges.isEmpty()) {
      return new ArrayList<>();
    }
    double finishedProportion =
        (double) (challenges.size() - nonfinished.size()) / challenges.size();
    boolean acceptMedium = finishedProportion >= 1D / 3;
    boolean acceptHigh = finishedProportion >= 2D / 3;

    List<Challenge> filteredChallenges = new ArrayList<>();
    for (Challenge challenge : nonfinished) {
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
}
