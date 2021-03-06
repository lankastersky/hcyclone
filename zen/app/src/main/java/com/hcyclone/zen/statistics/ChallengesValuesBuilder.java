package com.hcyclone.zen.statistics;

import android.content.Context;

import com.hcyclone.zen.App;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ChallengesValuesBuilder {

  private static final int CHALLENGES_REDUCE_BY_DATE_SIZE = 14;
  private static final DateFormat CHALLENGES_TIME_BY_DAYS_DATE_FORMAT =
      SimpleDateFormat.getDateInstance(DateFormat.SHORT);
  private static final DateFormat CHALLENGES_TIME_BY_WEEKS_DATE_FORMAT =
      new SimpleDateFormat("w yy");
  private static final DateFormat CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT =
      new SimpleDateFormat("MMM yy");
  private static final DateFormat CHALLENGES_TIME_BY_YEARS_DATE_FORMAT =
      new SimpleDateFormat("yyyy");
  private static final DateFormat DOMAIN_BY_DAYS_DATE_FORMAT =
      CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_WEEKS_DATE_FORMAT =
      CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_MONTHS_DATE_FORMAT =
      CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_YEARS_DATE_FORMAT =
      CHALLENGES_TIME_BY_YEARS_DATE_FORMAT;

  Map<String, Integer> challengeTimeToNumber;
  Map<String, Float> challengeTimeToRates;
  List<String> challengesDates;
  private DateFormat mergeDateFormat = CHALLENGES_TIME_BY_YEARS_DATE_FORMAT;
  private DateFormat domainDateFormat = DOMAIN_BY_YEARS_DATE_FORMAT;

  public int getValuesSize() {
    return challengesDates.size();
  }

  public double getMaxChallengesRate() {
//    float maxChallengeRate = 0;
//    for (Float rate : challengeTimeToRates.values()) {
//      if (maxChallengeRate < rate) {
//        maxChallengeRate = rate;
//      }
//    }
    return 100;
  }

  public double getMaxChallengesNumber() {
//    return ChallengeModel.getInstance().getShownChallengesNumber();
    int maxChallengeNumber = 0;
    for (Integer challengeNumber : challengeTimeToNumber.values()) {
      if (maxChallengeNumber < challengeNumber) {
        maxChallengeNumber = challengeNumber;
      }
    }
    return maxChallengeNumber;
  }

  public void build(List<Challenge> plotChallenges, Context context) {
    initReduceByDateType(plotChallenges);

    challengeTimeToNumber = new LinkedHashMap<>();
    challengeTimeToRates = new LinkedHashMap<>();
    challengesDates = new ArrayList<>();
    for (Challenge challenge : plotChallenges) {
      /*
       * TODO: we loose statistics if the challenge was taken before.
       * To solve this, we need to use {@link Challenge.getPrevFinishedTimes()}.
       */
      long finishedTime = challenge.getFinishedTime();
      Date date = new Date(finishedTime);
      String dateString = mergeDateFormat.format(date);
      if (challengeTimeToNumber.containsKey(dateString)) {
        int challengesNumber = challengeTimeToNumber.get(dateString);
        challengeTimeToNumber.put(dateString, ++challengesNumber);
        float challengesRate = challengeTimeToRates.get(dateString);
        /*
         * TODO: we loose statistics if the challenge was taken before.
         * To solve this, we need to use {@link Challenge.getPrevRatings()}.
         */
        challengeTimeToRates.put(dateString, challengesRate + challenge.getRating());
      } else {
        challengeTimeToNumber.put(dateString, 1);
        challengesDates.add(domainDateFormat.format(date));
        challengeTimeToRates.put(dateString, challenge.getRating());
      }
    }

    ChallengeModel challengeModel = ((App) context.getApplicationContext()).getChallengeModel();

    // Normalize rates.
    for (String dateString : challengeTimeToRates.keySet()) {
      int challengesNumber = challengeTimeToNumber.get(dateString);
      float challengesRate = challengeTimeToRates.get(dateString);
      float normalizedRate =
          challengesRate * 100 // in %
              / challengesNumber
              / challengeModel.getMaxRating(context);
      normalizedRate = (float) Math.round(normalizedRate * 10) / 10; // round up to 1 digit.
      challengeTimeToRates.put(dateString, normalizedRate);
    }
  }

  private void initReduceByDateType(List<Challenge> challenges) {
    MergeDatesType mergeDatesType = MergeDatesType.YEARS;
    if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE) {
      mergeDatesType = MergeDatesType.DAYS;
      mergeDateFormat = CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
      domainDateFormat = DOMAIN_BY_DAYS_DATE_FORMAT;
    } else if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE * 7) {
      mergeDatesType = MergeDatesType.WEEKS;
      mergeDateFormat = CHALLENGES_TIME_BY_WEEKS_DATE_FORMAT;
      domainDateFormat = DOMAIN_BY_WEEKS_DATE_FORMAT;
    } else if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE * 30) {
      mergeDatesType = MergeDatesType.MONTHS;
      mergeDateFormat = CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT;
      domainDateFormat = DOMAIN_BY_MONTHS_DATE_FORMAT;
    }
  }

  enum MergeDatesType {
    DAYS,
    WEEKS,
    MONTHS,
    YEARS
  }
}
