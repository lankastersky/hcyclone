package com.hcyclone.zen.model;

import java.util.List;

/** Modifiable challenge data for serialization etc. */
public final class ChallengeData {

  public final String id;
  public final int status;
  public final long finishedTime;
  public final float rating;
  public final String comments;
  public final List<Integer> prevStatuses;
  public final List<Long> prevFinishedTimes;
  public final List<Float> prevRatings;

  public ChallengeData(
      String id,
      int status,
      long finishedTime,
      float rating,
      String comments,
      List<Integer> prevStatuses,
      List<Long> prevFinishedTimes,
      List<Float> prevRatings) {

    this.id = id;
    this.status = status;
    this.finishedTime = finishedTime;
    this.rating = rating;
    this.comments = comments;
    this.prevStatuses = prevStatuses;
    this.prevFinishedTimes = prevFinishedTimes;
    this.prevRatings = prevRatings;
  }
}
