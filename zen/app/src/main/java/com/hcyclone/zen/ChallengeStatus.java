package com.hcyclone.zen;

public class ChallengeStatus {

  public final String id;
  public final int status;
  public final long finishedTime;

  public ChallengeStatus(String id, int status, long finishedTime) {
    this.id = id;
    this.status = status;
    this.finishedTime = finishedTime;
  }
}
