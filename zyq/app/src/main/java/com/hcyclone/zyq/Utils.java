package com.hcyclone.zyq;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.text.Html;
import android.text.Spanned;

import com.crashlytics.android.Crashlytics;
import com.google.common.io.ByteStreams;
import com.hcyclone.zyq.view.DescriptionActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class Utils {

  private static final String TAG = Utils.class.getSimpleName();

  private Utils() {}

  public static boolean isDebug() {
    return BuildConfig.DEBUG;
  }

  public static int pxToDp(int px) {
    return (int) (px / Resources.getSystem().getDisplayMetrics().density);
  }

  public static int dpToPx(int dp) {
    return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
  }

  public static void showDescription(String description, Context context) {
    Intent intent = new Intent(context, DescriptionActivity.class);
    intent.putExtra(BundleConstants.DESCRIPTION_KEY, description);
    context.startActivity(intent);
  }

  public static Spanned fromHtml(String text) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
     return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
    } else {
      return Html.fromHtml(text);
    }
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

  /** Opens Youtube video in the app. */
  public static void watchYoutubeVideo(String videoUrl, Context context) {
      Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
      context.startActivity(webIntent);
  }

  public static void sendFeedback(Context context) {
    String subject = getApplicationName(context) + " feedback "
        + getVersionName(context) + " (" + getVersionCode(context) + ")";
    String email = "lankastersky@gmail.com";
    Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
        Uri.parse("mailto:" + email + "?subject=" + subject));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    context.startActivity(Intent.createChooser(emailIntent,
        context.getString(R.string.feedback_send_email)));
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
      Crashlytics.logException(e);
      Log.e(TAG, "Can't get package info");
      return true;
    }
  }

  public static boolean isInternetConnected(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  public static String readFromAssets(String fileName, Context context) throws IOException {
    InputStream inputStream = context.getAssets().open(fileName);
    return new String(ByteStreams.toByteArray(inputStream));
  }

  public static String readFromRawResources(int resId, Context context) throws IOException {
    InputStream inputStream = context.getResources().openRawResource(resId);
    return new String(ByteStreams.toByteArray(inputStream));
  }

  public static String getVersionName(Context context) {
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

  private static String getApplicationName(Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0
        ? applicationInfo.nonLocalizedLabel.toString()
        : context.getString(stringId);
  }

  public static int getVersionCode(Context context) {
    try {
      return context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (Exception e) {
      Log.e(TAG, "Couldn't get the version code.", e);
      return 0;
    }
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
