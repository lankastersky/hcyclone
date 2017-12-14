package com.hcyclone.zen;

import android.app.Application;
import android.os.StrictMode;

import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.AlarmService;
import com.hcyclone.zen.service.NotificationService;
import com.hcyclone.zen.service.PreferencesService;

public class App extends Application {

  private static final String TAG = App.class.getSimpleName();

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

  @Override
  public void onCreate() {
    super.onCreate();
    initSingletons();
    if (Utils.isDebug()) {
      enableStrictMode();
    }
    registerActivityLifecycleCallbacks(AppLifecycleManager.getInstance());
  }

  protected void initSingletons() {
    ChallengeModel.getInstance().init(this);
    AlarmService.getInstance().init(this);
    NotificationService.getInstance().init(this);
    PreferencesService.getInstance().init(this);
    Analytics.getInstance().init(this);
    AppLifecycleManager.getInstance().init(this);
  }
}
