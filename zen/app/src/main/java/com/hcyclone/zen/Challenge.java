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

  // Types
  public static final String TYPE_STOP_INTERNAL_DIALOG = "SID";

  // Levels
  public static final int LEVEL_EASY = 1;

  public String id;
  //public final String title;
  private String content;
  private String details;
  private String type;
  private int level;
  private String source;
  private String citate;

  private int status;
  private long finishedTime;

  public Challenge() {
    this.status = UNKNOWN;
  }

  public Challenge(String id, String content, String details, String type, long level,
                   String source, String citate) {
    this.id = id;
    this.content = content;
    this.details = details;
    this.type = type;
    this.level = (int) level;
    this.source = source;
    this.citate = citate;
    this.status = UNKNOWN;
  }

  public String getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public String getDetails() {
    return details;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public int getLevel() {
    return level;
  }

  public String getSource() {
    return source;
  }

  public String getCitate() {
    return citate;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void setFinishedTime(long finishedTime) {
    this.finishedTime = finishedTime;
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
