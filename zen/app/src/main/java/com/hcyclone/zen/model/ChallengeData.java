package com.hcyclone.zen.model;

/** Modifiable challenge data for serialization etc. */
public class ChallengeData {

  public final String id;

  public final int status;
  public final long finishedTime;
  public final float rating;
  public final String comments;

  public ChallengeData(String id, int status, long finishedTime, float rating, String comments) {
    this.id = id;
    this.status = status;
    this.finishedTime = finishedTime;
    this.rating = rating;
    this.comments = comments;
  }
}
