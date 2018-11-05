package com.hcyclone.zyq;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.hcyclone.zyq.model.ExerciseModel;
import com.hcyclone.zyq.service.FeaturesService;

public class App extends MultiDexApplication {

  private static final String TAG = App.class.getSimpleName();

  private AudioPlayer player;
  private ExerciseModel exerciseModel;

  @Override
  public void onCreate() {
    super.onCreate();

    if (Utils.isDebug()) {
      enableStrictMode();
    }

    initSingletons();
    exerciseModel = new ExerciseModel(this);
    player = new AudioPlayer();
  }

  public AudioPlayer getPlayer() {
    return player;
  }

  public ExerciseModel getExerciseModel() {
    return exerciseModel;
  }

  protected void initSingletons() {
    MobileAds.initialize(this, getString(R.string.admob_app_id));

    Analytics.getInstance().init(this);
    FeaturesService.getInstance().init(this);
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
