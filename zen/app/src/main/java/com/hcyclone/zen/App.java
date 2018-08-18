package com.hcyclone.zen;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.AlarmService;
import com.hcyclone.zen.service.FeaturesService;
import com.hcyclone.zen.service.NotificationService;

public class App extends MultiDexApplication {

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
    MobileAds.initialize(this, getString(R.string.admob_app_id));

    ChallengeModel.getInstance().init(this);
    AlarmService.getInstance().init(this);
    NotificationService.getInstance().init(this);
    Analytics.getInstance().init(this);
    AppLifecycleManager.getInstance().init(this);
    FeaturesService.getInstance().init(this);
  }
}
