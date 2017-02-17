package com.hcyclone.zen;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;

public class App extends Application {

  private static final String TAG = App.class.getSimpleName();

  @Override
  public void onCreate() {
    super.onCreate();
    initSingletons();
    if (Utils.getInstance().isDebug()) {
      enableStrictMode();
    }
    registerActivityLifecycleCallbacks(new AppLifecycleManager(this));
  }

  protected void initSingletons() {
    ChallengeModel.getInstance().init(this);
    AlarmService.getInstance().init(this);
    NotificationService.getInstance().init(this);
    PreferencesService.getInstance().init(this);
    Utils.getInstance().init(this);
    Analytics.getInstance().init(this);
  }

  private static void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()
        .penaltyLog()
        .penaltyFlashScreen()
        .build());

    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectActivityLeaks()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .detectLeakedRegistrationObjects()
        .penaltyLog()
        .build());

    Log.d(TAG, "Strict mode enabled");
  }
}
