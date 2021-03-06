package com.hcyclone.zen.model;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import com.google.common.collect.Iterables;
import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.App;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.ChallengeArchiver;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge.LevelType;
import com.hcyclone.zen.model.Challenge.StatusType;
import com.hcyclone.zen.service.FeaturesService;

import com.hcyclone.zen.service.FeaturesService.FeaturesType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChallengeModel {

  private static final Calendar CALENDAR = Calendar.getInstance();

  private static final String TAG = ChallengeModel.class.getSimpleName();

  private static final double PROBABILITY_GET_DECLINED_CHALLENGE = 1D / 6;
  private static final double MEDIUM_LEVEL_CHALLENGES_RATIO = 1D / 3;
  private static final double HIGH_LEVEL_CHALLENGES_RATIO = 2D / 3;

  private final Map<String, Challenge> challengeMap = new HashMap<>();
  private final ChallengeArchiver challengeArchiver;
  private final FeaturesService featuresService;

  private String currentChallengeId;
  private long currentChallengeShownTime;
  private int level;

  public ChallengeModel(@NonNull Context context) {
    challengeArchiver = new ChallengeArchiver(context, AppLifecycleManager.getInstance());
    featuresService = ((App)context.getApplicationContext()).getFeaturesService();
  }

  @Challenge.LevelType
  public int getLevel() {
    return level;
  }

  private void setLevel(int level) {
    this.level = level;
  }

  public Challenge getCurrentChallenge() {
    return getChallengesMap().get(currentChallengeId);
  }

  public List<Challenge> getFinishedChallengesSortedDesc() {
    List<Challenge> challenges = getFinishedChallenges();
    // Sort by finished time in reverse order.
    Collections.sort(challenges, (Challenge challenge1, Challenge challenge2) ->
      -Long.compare(challenge1.getFinishedTime(), challenge2.getFinishedTime())
    );
    return challenges;
  }

  public List<Challenge> getFinishedChallengesSorted() {
    List<Challenge> challenges = getFinishedChallenges();
    // Sort by finished time.
    Collections.sort(challenges, (Challenge challenge1, Challenge challenge2) ->
        Long.compare(challenge1.getFinishedTime(), challenge2.getFinishedTime())
    );
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
    int shown = getChallengesByStatus(Challenge.SHOWN).size();
    return shown + getFinishedChallenges().size();
  }

  public List<Challenge> getFinishedChallenges() {
    return getChallengesByStatus(Challenge.FINISHED);
  }

  public List<Challenge> getChallenges() {
    ArrayList<Challenge> challenges = new ArrayList<>(getChallengesMap().values());
    // Sort by id.
    Collections.sort(challenges, (Challenge challenge1, Challenge challenge2) ->
        challenge1.getId().compareTo(challenge2.getId())
    );
    return challenges;
  }

  /** Gets persistently stored current challenge. */
  public Challenge getSerializedCurrentChallenge() {
    return challengeArchiver.restoreCurrentChallenge();
  }

  public Challenge getChallenge(String challengeId) {
    return challengeMap.get(challengeId);
  }

  public void loadChallenges() {
    Log.d(TAG, "Load challenges");
    if (challengeMap.isEmpty()) {
      List<Challenge> challenges = challengeArchiver.restoreChallenges();
      for (Challenge challenge : challenges) {
        challengeMap.put(challenge.getId(), challenge);
      }
    }
    // Update loaded challenges with history data.
    selectChallengeIfNeeded();
  }

  public void saveChallenges(List<Challenge> challenges) {
    Log.d(TAG, "Save challenges");
    challengeMap.clear();
    // if (Utils.isDebug()) {
    //   challenges = generateChallenges();
    // }
    for (Challenge challenge : challenges) {
      challengeMap.put(challenge.getId(), challenge);
    }
    challengeArchiver.storeChallenges(challenges);
  }

  /**
   * Sets challenge status as {@link Challenge#SHOWN} if current status is
   * {@link Challenge#UNKNOWN}.
   */
  public void setChallengeShown(String challengeId) {
    if (!challengeId.equals(currentChallengeId)) {
      Log.w(
          TAG, "Can't mark challenge shown. ChallengeId is not current challenge: " + challengeId);
      return;
    }
    if (getCurrentChallenge().getStatus() != Challenge.UNKNOWN) {
      return;
    }
    currentChallengeShownTime = new Date().getTime();
    updateCurrentChallenge();
    storeState();
  }

  /** Sets challenge status as {@link Challenge#ACCEPTED}. */
  public void setChallengeAccepted(String challengeId) {
    if (!challengeId.equals(currentChallengeId)) {
      Log.w(
          TAG, "Can't accept challenge. ChallengeId is not current challenge: " + challengeId);
      return;
    }
    updateCurrentChallenge();
    storeState();
  }

  /** Sets challenge status as {@link Challenge#FINISHED}. */
  public void setChallengeFinished(String challengeId) {
    if (!challengeId.equals(currentChallengeId)) {
      Log.w(
          TAG, "Can't finish challenge. ChallengeId is not current challenge: " + challengeId);
      return;
    }
    updateCurrentChallenge();
    storeState();
  }

  /** Challenge is ready to be accepted before 6pm. */
  public boolean isTimeToAcceptChallenge() {
    CALENDAR.setTimeInMillis(currentChallengeShownTime);
    return Utils.isTimeLess6pm(CALENDAR);
  }

  /** Challenge is ready to be finished today after 6pm. */
  public boolean isTimeToFinishChallenge() {
    boolean result = false;
    Challenge challenge = getChallengesMap().get(currentChallengeId);
    if (challenge.getStatus() == Challenge.ACCEPTED) {
      Date timeToFinish = Utils.get6PM(currentChallengeShownTime);
//      CALENDAR.setTimeInMillis(currentChallengeShownTime);
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

  /**
   * Checks if it's time for level up based on the number of finished challenges and stores the
   * upgraded level if needed.
   */
  public boolean checkLevelUp() {
    int[] upgradeToLevel = new int[1];
    if (challengesForLevelUp(upgradeToLevel) <= 0
        && upgradeToLevel[0] != Challenge.LEVEL_UNKNOWN
        && upgradeToLevel[0] != Challenge.LEVEL_LOW) {

      setLevel(upgradeToLevel[0]);
      challengeArchiver.storeLevel(getLevel());
      return true;
    }
    return false;
  }

  /**
   * Returns the number of challenges left for the level up. {@code upgradeToLevel} returns the
   * level to upgrade to.
   * The result can be negative for backward compatibility (the old version used other mechanism
   * to calculate the level-up).
   */
  public int challengesForLevelUp(int[] upgradeToLevel) {
    List<Challenge> finishedChallenges = getChallengesByStatus(Challenge.FINISHED);
    Collection<Challenge> challenges = getChallengesMap().values();
    int result = 0;
    if (upgradeToLevel != null) {
      upgradeToLevel[0] = Challenge.LEVEL_UNKNOWN;
    }
    switch (level) {
      case Challenge.LEVEL_LOW:
        int lowChallengesNumber = (int) (challenges.size() * MEDIUM_LEVEL_CHALLENGES_RATIO);
        result = lowChallengesNumber - finishedChallenges.size();
        if (upgradeToLevel != null) {
          upgradeToLevel[0] = Challenge.LEVEL_MEDIUM;
        }
        break;
      case Challenge.LEVEL_MEDIUM:
        int mediumChallengesNumber = (int) (challenges.size() * HIGH_LEVEL_CHALLENGES_RATIO);
        result = mediumChallengesNumber - finishedChallenges.size();
        if (upgradeToLevel != null) {
          upgradeToLevel[0] = Challenge.LEVEL_HIGH;
        }
        break;
      default:
        break;
    }
    return result;
  }

  private Map<String, Challenge> getChallengesMap() {
    return challengeMap;
  }

  @NonNull
  private List<Challenge> getChallengesByStatus(@StatusType int status) {
    List<Challenge> challenges = new ArrayList<>();
    for (Challenge challenge : getChallengesMap().values()) {
      if (challenge.getStatus() == status) {
        challenges.add(challenge);
      }
    }
    return challenges;
  }

  /** Challenge expires at midnight of this day. */
  private boolean isChallengeTimeExpired() {
    boolean result;
    Date timeToDecline = Utils.getNextMidnight(currentChallengeShownTime);
    CALENDAR.setTimeInMillis(currentChallengeShownTime);
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
    restoreState();

    boolean newChallengeRequired = false;
    if (TextUtils.isEmpty(currentChallengeId)) {
      newChallengeRequired = true;
    } else {
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
            Analytics.getInstance().sendChallengeStatus(challenge);
            newChallengeRequired = true;
          }
          break;
        case Challenge.ACCEPTED:
          if (isChallengeTimeExpired()) {
            // User is interested in challenge. Give him a chance to finish it next time.
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
    }
    if (newChallengeRequired) {
      currentChallengeId = getNewChallengeId();
    }
    Challenge currentChallenge = getChallengesMap().get(currentChallengeId);
    if (currentChallenge == null) {
      Log.e(TAG, "Failed to get current challenge with id " + currentChallengeId);
      return;
    } else {
      Log.d(TAG, "Current challenge id is " + currentChallengeId + ": "
          + currentChallenge.getContent());
    }

    storeState();
  }

  /**
   * If there are nonfinished challenges, get random challenge from them taking into account levels.
   * Else if there are declined challenges, get random challenge from them with some probability.
   * Else get random challenge from all not equal to previous one.
   */
  @NonNull
  private String getNewChallengeId() {
    String challengeId = "";
    Challenge challenge = null;
    List<Challenge> nonfinishedChallenges = filterNonfinishedChallengesByLevel();

    // Only challenges of first level are available for free version.
    if (featuresService.getFeaturesType() == FeaturesType.FREE) {
      nonfinishedChallenges = filterChallengesByLevel(nonfinishedChallenges, Challenge.LEVEL_LOW);
    }

    if (nonfinishedChallenges.size() > 0) {
      challenge = getRandomChallenge(nonfinishedChallenges);
      challengeId = challenge.getId();
    } else {
      List<Challenge> declinedChallenges = getChallengesByStatus(Challenge.DECLINED);
      if (declinedChallenges.size() > 0 && Math.random() < PROBABILITY_GET_DECLINED_CHALLENGE) {
        // Don't force user to take a declined challenge again. Show declined challenges with some
        // probability.
        Log.d(TAG, "Assign random declined challenge");
        challenge = getRandomChallenge(declinedChallenges);
        challengeId = challenge.getId();
      } else if (!getChallengesMap().isEmpty()) {
        // All available challenges are finished. Return a random old one.
        Collection<Challenge> challenges = getChallengesMap().values();
        challenge = getRandomChallenge(challenges);
        Challenge currentChallenge = getCurrentChallenge();
        if (currentChallenge != null && challenges.size() > 1) {
          while (challenge.getId().equals(currentChallenge.getId())) {
            challenge = getRandomChallenge(challenges);
          }
        }
        challengeId = challenge.getId();
      }
    }
    if (challenge != null) {
      if (challenge.backup()) {
        Analytics.getInstance().sendChallengeBackupData(challenge);
      }
      challenge.reset();
    }
    return challengeId;
  }

  private Challenge getRandomChallenge(@NonNull Collection<Challenge> challenges) {
    if (challenges.isEmpty()) {
      return null;
    }
    int id = (int) (Math.random() * challenges.size()); // Round up to floor value.
    return Iterables.get(challenges, id);
  }

  @NonNull
  private List<Challenge> filterChallengesByLevel(
      List<Challenge> challenges, @LevelType int level) {

    List<Challenge> filteredChallenges = new ArrayList<>();
    for (Challenge challenge : challenges) {
      if (challenge.getLevel() == level) {
        filteredChallenges.add(challenge);
      }
    }
    return filteredChallenges;
  }

  /**
   * Low-level challenges are available from the beginning.
   * Medium-level challenges are available when 1/3 of challenges are finished.
   * High-level challenges are available when 2/3 of challenges are finished.
   */
  private List<Challenge> filterNonfinishedChallengesByLevel() {
    Collection<Challenge> challenges = getChallengesMap().values();
    if (challenges.isEmpty()) {
      return new ArrayList<>();
    }
    List<Challenge> nonfinished = getNonfinishedChallenges();
    double finishedProportion =
        (double) (challenges.size() - nonfinished.size()) / challenges.size();
    boolean acceptMedium = finishedProportion >= MEDIUM_LEVEL_CHALLENGES_RATIO;
    boolean acceptHigh = finishedProportion >= HIGH_LEVEL_CHALLENGES_RATIO;

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

  private List<Challenge> getNonfinishedChallenges() {
    List<Challenge> nonfinished = getChallengesByStatus(Challenge.UNKNOWN);
    // Ideally, we shouldn't have such challenges.
    nonfinished.addAll(getChallengesByStatus(Challenge.ACCEPTED));
    // Ideally, we shouldn't have such challenges.
    nonfinished.addAll(getChallengesByStatus(Challenge.SHOWN));
    return nonfinished;
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

  private void storeState() {
    challengeArchiver.storeChallengeData(challengeMap);
    challengeArchiver.storeCurrentChallenge(getCurrentChallenge());
    challengeArchiver.storeCurrentChallengeShownTime(currentChallengeShownTime);
    challengeArchiver.storeLevel(level);
  }

  private void updateCurrentChallenge() {
    Log.d(TAG, "updateCurrentChallenge");
    Challenge challenge = getCurrentChallenge();
    challenge.updateStatus();
    Analytics.getInstance().sendChallengeStatus(challenge);
  }

  private List<Challenge> generateChallenges() {
    List<Challenge> challenges = new ArrayList<>();
//      int days = 1;
      int days = 3;
//      int days = 13;
//      int days = 14;
//      int days = 14 * 7 - 1;
//      int days = 14 * 7;
//      int days = 14 * 30 - 1;
//    int days = 14 * 30;
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
//      int status = Challenge.FINISHED;
//      do {
//        status = (int) (Math.random() * Challenge.STATUSES_LENGTH);
//      } while (status == Challenge.ACCEPTED);
//      challenge.setStatus(status);
//      challenge.setRating((float) Math.random() * getMaxRating(context));
//      challenge.setRating(i % 5);
//      CALENDAR.setTime(new Date());
//      CALENDAR.add(Calendar.DAY_OF_MONTH, (int) -(Math.random() * days));
      //CALENDAR.add(Calendar.DAY_OF_YEAR, i + 1);
//      challenge.setFinishedTime(CALENDAR.getTimeInMillis());
      challenges.add(challenge);
    }
    return challenges;
  }
}
