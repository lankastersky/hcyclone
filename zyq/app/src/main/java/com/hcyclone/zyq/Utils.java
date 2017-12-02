package com.hcyclone.zyq;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.AlarmClock;

import com.hcyclone.zyq.view.DescriptionActivity;

public final class Utils {

  private static final String TAG = Utils.class.getSimpleName();

  private Utils() {}

  static boolean isDebug() {
    return BuildConfig.DEBUG;
  }

  public static void showDescription(Context context) {
    Intent intent = new Intent(context, DescriptionActivity.class);
    //intent.putExtra(BundleConstants.EXERCISE_ID_KEY, exerciseModel)
    context.startActivity(intent);
  }

  public static void startTimer(String message, Context context) {
    Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
        .putExtra(AlarmClock.EXTRA_MESSAGE, message)
        //.putExtra(AlarmClock.EXTRA_LENGTH, seconds)
        .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
    if (intent.resolveActivity(context.getPackageManager()) != null) {
      context.startActivity(intent);
    }
  }

//  public static void playMedia(Uri file, Context context) {
//    Intent intent = new Intent(Intent.ACTION_VIEW);
//    intent.setData(file);
//    if (intent.resolveActivity(context.getPackageManager()) != null) {
//      context.startActivity(intent);
//    }
//  }

  public static void sendFeedback(Context context) {
    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
        "mailto","lankastersky@gmail.com", null));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getApplicationName(context) + " feedback "
        + getVersionName(context) + " (" + getVersionCode(context) + ")");

    context.startActivity(Intent.createChooser(emailIntent,
        context.getString(R.string.feedback_send_email)));
  }

  public static boolean isInternetConnected(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  private static String getApplicationName(Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0
        ? applicationInfo.nonLocalizedLabel.toString()
        : context.getString(stringId);
  }

  private static String getVersionName(Context context) {
    try {
      return context
          .getPackageManager()
          .getPackageInfo(context.getPackageName(), 0)
          .versionName;
    } catch (Exception e) {
      Log.e(TAG, "Couldn't get the version name.", e);
      return "";
    }
  }

  private static int getVersionCode(Context context) {
    try {
      return context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (Exception e) {
      Log.e(TAG, "Couldn't get the version code.", e);
      return 0;
    }
  }
}
