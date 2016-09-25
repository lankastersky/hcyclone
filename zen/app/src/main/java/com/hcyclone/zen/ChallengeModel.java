package com.hcyclone.zen;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeModel {

  private static final ChallengeModel instance = new ChallengeModel();

  public static final Map<String, Challenge> ITEM_MAP = new HashMap<>();
  private static final int COUNT = 25;

  private String mCurrentChallengeId;

  static {
    // Add some sample items.
    for (int i = 1; i <= COUNT; i++) {
      addItem(createDummyItem(i));
    }
  }

  public static ChallengeModel getInstance() {
    return instance;
  }

  public Challenge getCurrentChallenge() {
    Challenge challenge;
    if (TextUtils.isEmpty(mCurrentChallengeId)) {
      challenge = getRandomChallenge();
      mCurrentChallengeId = challenge.id;
    } else {
      challenge = getChallengesMap().get(mCurrentChallengeId);
    }
    if (challenge.getStatus() == Challenge.NONACCEPTED
        || challenge.getStatus() == Challenge.ACCEPTED) {
      return challenge;
    }
    List<Challenge> nonacceptedChallenges = getNonacceptedChallenges();
    if (nonacceptedChallenges.size() > 0) {
      challenge = nonacceptedChallenges.get(0);
      mCurrentChallengeId = challenge.id;
    } else {
      // All challenges are finished. Return random old one.
      challenge = getRandomChallenge();
      mCurrentChallengeId = challenge.id;
    }
    return challenge;
  }

  private Challenge getRandomChallenge() {
    int id = (int) (Math.random() * getChallengesMap().keySet().size());
    int i = 0;
    for (Challenge challenge : getChallengesMap().values()) {
      if (i++ == id) {
        return challenge;
      }
    }
    return null;
  }

  private List<Challenge> getNonacceptedChallenges() {
    return getChallengesMap(Challenge.NONACCEPTED);
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

  private List<Challenge> getChallengesMap(int status) {
    List<Challenge> challenges = new ArrayList<>();
    for (Challenge challenge : getChallengesMap().values()) {
      if (challenge.getStatus() == status) {
        challenges.add(challenge);
      }
    }
    return challenges;
  }

  public Map<String, Challenge> getChallengesMap() {
    return ITEM_MAP;
  }

  private static void addItem(Challenge item) {
    ITEM_MAP.put(item.id, item);
  }

  private static Challenge createDummyItem(int position) {
    return new Challenge(String.valueOf(position), "Challenge " + position, makeDetails(position));
  }

  private static String makeDetails(int position) {
    StringBuilder builder = new StringBuilder();
    builder.append("Details about Challenge: ").append(position);
    for (int i = 0; i < position; i++) {
      builder.append("\nMore details information here.");
    }
    return builder.toString();
  }

}
