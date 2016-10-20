package com.hcyclone.zen;

import android.util.Log;

import java.util.Date;

public class Challenge {

  private static final String TAG = Challenge.class.getSimpleName();

  public static final int UNKNOWN = 0;
  public static final int SHOWN = 1;
  public static final int ACCEPTED = 2;
  public static final int FINISHED = 3;
  public static final int DECLINED = 4;

  public String id;

  //public final String title;
  public String content;
  public String details;

  private int status;
  private long finishedTime;

  public Challenge() {
    this.status = UNKNOWN;
  }

  public Challenge(String id, String content, String details) {
    this.id = id;
    this.content = content;
    this.details = details;
    this.status = UNKNOWN;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  @Override
  public String toString() {
    return content;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void updateStatus() {
    Log.d(TAG, "Update status for challenge " + id + " from " + status);
    switch (status) {
      case UNKNOWN:
        status = SHOWN;
        break;
      case SHOWN:
        status = ACCEPTED;
        break;
      case ACCEPTED:
        status = FINISHED;
        finishedTime = new Date().getTime();
        break;
      default:
        Log.e(Challenge.class.getSimpleName(), "Wrong status to update: " + status);
        break;
    }
  }

  public void decline() {
    Log.d(TAG, "Decline challenge: " + id);
    switch (status) {
      case SHOWN:
      case ACCEPTED:
        status = DECLINED;
        finishedTime = new Date().getTime();
        break;
      default:
        Log.e(Challenge.class.getSimpleName(), "Wrong status to decline: " + status);
        break;
    }
  }
}
