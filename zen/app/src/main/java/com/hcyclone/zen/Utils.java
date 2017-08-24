package com.hcyclone.zen;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public final class Utils {

  private static final String TAG = Utils.class.getSimpleName();
  private static final Utils instance = new Utils();

  private Context context;

  private Utils() {}

  public static Utils getInstance() {
    return instance;
  }

  public static String getApplicationName(Context context) {
    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
  }

  public void init(@NonNull Context context) {
    this.context = context;
  }

  public boolean isDebug() {
    return BuildConfig.DEBUG;
  }

  public long getDebugAlarmRepeatTime() {
    return 10_000;
  }

  public long getDebugDailyAlarmTime() {
    return 50_000;
  }

  public boolean isTimeLess6pm(Calendar date) {
    if (isDebug()) {
      return true;
    }
    return date.get(Calendar.HOUR_OF_DAY) < 18;
  }

  public Date get6PM(long time) {
    // Today.
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(time);
    if (isDebug()) {
      date.add(Calendar.SECOND, 5);
    } else {
      date.set(Calendar.HOUR_OF_DAY, 18);
      // Reset minutes, seconds and millis.
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MILLISECOND, 0);
    }
    return date.getTime();
  }

  public Date getNextMidnight(long time) {
    // Today.
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(time);
    if (isDebug()) {
      date.add(Calendar.SECOND, 10);
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
    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
        "mailto","lankastersky@gmail.com", null));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getApplicationName(activityContext) + " feedback "
        + getVersionName() + " (" + getVersionCode() + ")");
    activityContext.startActivity(Intent.createChooser(emailIntent,
        activityContext.getString(R.string.feedback_send_email)));
  }

  public String getVersionName() {
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

  public Dialog buildDialog(String title, String text, Context context) {
    final Dialog dialog = new Dialog(context, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.alert_dialog);
    TextView titleView = (TextView) dialog.findViewById(R.id.alert_dialog_title);
    titleView.setText(title);
    TextView textView = (TextView) dialog.findViewById(R.id.alert_dialog_text);
    textView.setText(text);

    Button updateButton = (Button) dialog.findViewById(R.id.alert_dialog_button);
    updateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });
    return dialog;
  }
}
