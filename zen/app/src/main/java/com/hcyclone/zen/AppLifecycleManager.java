package com.hcyclone.zen;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/** Determines global app lifecycle states.
 *
 * The following is the reference of activities states:
 *
 * The <b>visible</b> lifetime of an activity happens between a call to onStart()
 * until a corresponding call to onStop(). During this time the user can see the
 * activity on-screen, though it may not be in the foreground and interacting with
 * the user. The onStart() and onStop() methods can be called multiple times, as
 * the activity becomes visible and hidden to the user.
 *
 * The <b>foreground</b> lifetime of an activity happens between a call to onResume()
 * until a corresponding call to onPause(). During this time the activity is in front
 * of all other activities and interacting with the user. An activity can frequently
 * go between the resumed and paused states -- for example when the device goes to
 * sleep, when an activity result is delivered, when a new intent is delivered --
 * so the code in these methods should be fairly lightweight.
 *
 * */
public class AppLifecycleManager implements ActivityLifecycleCallbacks {

  private static final String TAG = AppLifecycleManager.class.getSimpleName();

  /** Manages the state of opened vs closed activities, should be 0 or 1.
   * It will be 2 if this value is checked between activity B onStart() and
   * activity A onStop().
   * It could be greater if the top activities are not fullscreen or have
   * transparent backgrounds.
   */
  private static int visibleActivityCount = 0;

  /** Manages the state of opened vs closed activities, should be 0 or 1
   * because only one can be in foreground at a time. It will be 2 if this
   * value is checked between activity B onResume() and activity A onPause().
   */
  private static int foregroundActivityCount = 0;

  private Context context;

  public AppLifecycleManager(Context context) {
    this.context = context;
  }

  public void onActivityCreated(Activity activity, Bundle bundle) {
  }

  public void onActivityDestroyed(Activity activity) {
  }

  public void onActivityResumed(Activity activity) {
    foregroundActivityCount++;
  }

  public void onActivityPaused(Activity activity) {
    foregroundActivityCount--;
  }


  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  public void onActivityStarted(Activity activity) {
    visibleActivityCount++;

    if (visibleActivityCount == 1) {
      // Don't receive notifications if app is started.
      Log.d(TAG, "Disable alarm receiver");
      ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
      context.getPackageManager().setComponentEnabledSetting(receiver,
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);
    }
  }

  public void onActivityStopped(Activity activity) {
    visibleActivityCount--;

    if (visibleActivityCount == 0) {
      Log.d(TAG, "Enable alarm receiver");
      ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
      context.getPackageManager().setComponentEnabledSetting(receiver,
          PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
          PackageManager.DONT_KILL_APP);
    }
  }
}