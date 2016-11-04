package com.hcyclone.zen;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public final class Utils {

  private static final String TAG = Utils.class.getSimpleName();
  private static final Utils instance = new Utils();

  private Context context;

  private Utils() {
  }

  public static Utils getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
  }

  public static Date getMidnight(long time) {
    // Today.
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(time);
    if (BuildConfig.DEBUG) {
      date.add(Calendar.SECOND, 20);
    } else {
      // Reset hour, minutes, seconds and millis.
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MILLISECOND, 0);
      // Midnight of next day.
      date.add(Calendar.DAY_OF_MONTH, 1);
    }
    return date.getTime();
  }

  public void sendFeedback(Context activityContext) {
    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, "lankastersky@gmail.com");
    intent.putExtra(Intent.EXTRA_SUBJECT, "Zen feedback " + getVersionName()
        + " (" + getVersionCode() + ")");
//    intent.putExtra(Intent.EXTRA_TEXT, message);
    activityContext.startActivity(Intent.createChooser(intent, "Send bug report via email"));
  }

  private String getVersionName() {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (Exception e) {
      Log.e(TAG, "Couldn't get the version name.", e);
      return "";
    }
  }

  private int getVersionCode() {
    try {
      return context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (Exception e) {
      Log.e(TAG, "Couldn't get the version code.", e);
      return 0;
    }
  }

  /**
   * Checks network connectivity.
   */
  public boolean isConnected() {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }
}
