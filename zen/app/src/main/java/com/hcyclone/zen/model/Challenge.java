package com.hcyclone.zen.model;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.Log;

import java.util.Date;

public class Challenge {

  public static final int UNKNOWN = 0;
  public static final int SHOWN = 1;
  public static final int ACCEPTED = 2;
  public static final int FINISHED = 3;
  public static final int DECLINED = 4;

  // Levels
  public static final int LEVEL_LOW = 1;
  public static final int LEVEL_MEDIUM = 2;
  public static final int LEVEL_HIGH = 3;
  private static final String TAG = Challenge.class.getSimpleName();
  private String id;
  private String content;
  private String details;
  private @LevelType
  int level;
  private String quote;
  private String source;
  private String type;
  private String url;
  private float rating;
  private int status;
  private long finishedTime;

  public Challenge(String id, String content, String details, String type, long level,
                   String source, String url, String quote) {
    this.id = id;
    this.content = content;
    this.details = details;
    this.type = type;
    this.level = (int) level;
    this.source = source;
    this.url = url;
    this.quote = quote;
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

  public String getSourceAsHtml() {
    if (!TextUtils.isEmpty(source) && !TextUtils.isEmpty(url)) {
      return String.format("<a href='%s'>%s</a>", url, source);
    }
    return "";
  }

  public String getQuote() {
    return quote;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void setFinishedTime(long finishedTime) {
    this.finishedTime = finishedTime;
  }

  public float getRating() {
    return rating;
  }

  public void setRating(float rating) {
    this.rating = rating;
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
    Analytics.getInstance().sendChallengeStatus(this);
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
    Analytics.getInstance().sendChallengeStatus(this);
  }

  public void reset() {
    status = UNKNOWN;
    finishedTime = 0;
    rating = 0;
  }

  @IntDef({LEVEL_LOW, LEVEL_MEDIUM, LEVEL_HIGH})
  public @interface LevelType {
  }
}
