package com.hcyclone.zen.model;

/**
 * Stores modifiable challenge data.
 */
public class ChallengeData {

  public final String id;
  public final int status;
  public final long finishedTime;
  public final float rating;

  public ChallengeData(String id, int status, long finishedTime, float rating) {
    this.id = id;
    this.status = status;
    this.finishedTime = finishedTime;
    this.rating = rating;
  }
}
