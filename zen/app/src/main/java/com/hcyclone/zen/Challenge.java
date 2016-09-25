package com.hcyclone.zen;

import android.util.Log;

import java.util.Date;

public class Challenge {

  public static final int NONACCEPTED = 0;
  public static final int ACCEPTED = 1;
  public static final int FINISHED = 2;
//  public static final int DECLINED = 3;

  public final String id;
  //public final String title;
  public final String content;
  public final String details;

  private int status;
  private long finishedTime;

  public Challenge(String id, String content, String details) {
    this.id = id;
    this.content = content;
    this.details = details;
    this.status = NONACCEPTED;
  }

  @Override
  public String toString() {
    return content;
  }

  public int getStatus() {
    return status;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void updateStatus() {
    switch (status) {
      case NONACCEPTED:
        status = ACCEPTED;
        break;
      case ACCEPTED:
        status = FINISHED;
        finishedTime = new Date().getTime();
      default:
        Log.e(Challenge.class.getSimpleName(), "Wrong status");
        break;
    }
  }
}
