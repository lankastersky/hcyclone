package com.hcyclone.zyq;

import android.app.Application;
import android.os.StrictMode;

public class App extends Application {

  private static final String TAG = App.class.getSimpleName();

  @Override
  public void onCreate() {
    super.onCreate();
    initSingletons();
    if (Utils.getInstance().isDebug()) {
      enableStrictMode();
    }
  }

  protected void initSingletons() {
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
