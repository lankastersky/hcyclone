package com.hcyclone.zen;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.backup.BackupManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

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

  private static final AppLifecycleManager instance = new AppLifecycleManager();

  /** Manages the state of opened vs closed activities, should be 0 or 1.
   * It will be 2 if this value is checked between activity B onStart() and
   * activity A onStop().
   * It could be greater if the top activities are not fullscreen or have
   * transparent backgrounds.
   */
  private static int visibleActivityCount = 0;

  private Context context;

  public static AppLifecycleManager getInstance() {
    return instance;
  }

  /** Returns true if any activity of app is visible (or device is sleep when
   * an activity was visible) */
  public static boolean isAppVisible() {
    return visibleActivityCount > 0;
  }

  public void init(@NonNull Context context) {
    this.context = context;
  }

  public void onActivityCreated(Activity activity, Bundle bundle) {}

  public void onActivityDestroyed(Activity activity) {}

  public void onActivityResumed(Activity activity) {}

  public void onActivityPaused(Activity activity) {}


  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  public void onActivityStarted(Activity activity) {
    visibleActivityCount++;
  }

  public void onActivityStopped(Activity activity) {
    visibleActivityCount--;
  }

  public void requestBackup() {
    BackupManager bm = new BackupManager(context);
    bm.dataChanged();
  }
}