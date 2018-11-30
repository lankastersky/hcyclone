package com.hcyclone.zen;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.hcyclone.zen.model.Challenge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Utils {

  private static final String TAG = Utils.class.getSimpleName();
  private static final Calendar CALENDAR = Calendar.getInstance();
  private static final DateFormat SIMPLE_DATE_FORMAT = SimpleDateFormat.getDateInstance();

  private Utils() {}

  public static String getApplicationName(Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0
        ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
  }

  public static boolean isDebug() {
    return BuildConfig.DEBUG;
  }

  public static long getDebugAlarmRepeatTime() {
    return 15_000;
  }

  public static long getDebugDailyAlarmTime() {
    return 5_000;
  }

  public static boolean isTimeLess6pm(Calendar date) {
    if (isDebug()) {
      return true;
    }
    return date.get(Calendar.HOUR_OF_DAY) < 18;
  }

  public static Date get6PM(long time) {
    // Today.
    CALENDAR.setTimeInMillis(time);
    if (isDebug()) {
      CALENDAR.add(Calendar.SECOND, 10);
    } else {
      CALENDAR.set(Calendar.HOUR_OF_DAY, 18);
      // Reset minutes, seconds and millis.
      CALENDAR.set(Calendar.MINUTE, 0);
      CALENDAR.set(Calendar.SECOND, 0);
      CALENDAR.set(Calendar.MILLISECOND, 0);
    }
    return CALENDAR.getTime();
  }

  public static Date getNextMidnight(long time) {
    // Today.
    CALENDAR.setTimeInMillis(time);
    if (isDebug()) {
      CALENDAR.add(Calendar.SECOND, 15);
    } else {
      // Reset hour, minutes, seconds and millis.
      CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
      CALENDAR.set(Calendar.MINUTE, 0);
      CALENDAR.set(Calendar.SECOND, 0);
      CALENDAR.set(Calendar.MILLISECOND, 0);
      // Midnight of next day.
      CALENDAR.add(Calendar.DAY_OF_MONTH, 1);
    }
    return CALENDAR.getTime();
  }

  public static void sendFeedback(Context context) {
    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
        "mailto","lankastersky@gmail.com", null));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getApplicationName(context) + " feedback "
        + getVersionName(context) + " (" + getVersionCode(context) + ")");
    context.startActivity(Intent.createChooser(emailIntent,
        context.getString(R.string.feedback_send_email)));
  }

  public static String getVersionName(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (Exception e) {
      Log.e(TAG, "Can't get the version name.", e);
      Crashlytics.logException(e);
      return "";
    }
  }

  public static int getVersionCode(Context context) {
    try {
      return context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (Exception e) {
      Log.e(TAG, "Can't get the version code.", e);
      Crashlytics.logException(e);
      return 0;
    }
  }

  /**
   * Checks network connectivity.
   */
  public static boolean isOnline(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  /** Returns true if it's a first install or it's impossible to determine. */
  public static boolean isFirstInstall(Context context) {
    try {
      String packageName = context.getPackageName();
      PackageManager packageManager = context.getPackageManager();
      long firstInstallTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
      long lastUpdateTime = packageManager.getPackageInfo(packageName, 0).lastUpdateTime;
      return firstInstallTime == lastUpdateTime;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Can't get package info");
      Crashlytics.logException(e);
      return true;
    }
  }

  /** Builds dialog with OK button. */
  public static Dialog buildDialog(
      String title, String text, Context context, View.OnClickListener listener) {
    final Dialog dialog = new Dialog(context, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.alert_dialog);
    TextView titleView = dialog.findViewById(R.id.alert_dialog_title);
    titleView.setText(title);
    TextView textView = dialog.findViewById(R.id.alert_dialog_text);
    textView.setText(text);

    Button updateButton = dialog.findViewById(R.id.alert_dialog_button);
    if (listener == null) {
      updateButton.setOnClickListener((v) -> dialog.dismiss());
    } else {
      updateButton.setOnClickListener(listener);
    }
    return dialog;
  }

  public static String localizedChallengeLevel(@Challenge.LevelType int level, Context context) {
    String result = "";
    switch (level) {
      case Challenge.LEVEL_LOW:
        result = context.getString(R.string.challenge_level_low);
        break;
      case Challenge.LEVEL_MEDIUM:
        result = context.getString(R.string.challenge_level_medium);
        break;
      case Challenge.LEVEL_HIGH:
        result = context.getString(R.string.challenge_level_high);
        break;
      default:
        break;
    }
    return result;
  }

  public static String timeToString(long time) {
    return timeToString(time, SIMPLE_DATE_FORMAT);
  }

  public static String timeToString(long time, DateFormat dateFormat) {
    Date date = new Date(time);
    return dateFormat.format(date);
  }

  public static Locale getCurrentLocale(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return context.getResources().getConfiguration().getLocales().get(0);
    } else {
      //noinspection deprecation
      return context.getResources().getConfiguration().locale;
    }
  }
}
