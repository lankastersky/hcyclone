package com.hcyclone.zyq;

import android.app.Application;
import android.os.StrictMode;

import com.hcyclone.zyq.model.ExerciseModel;

public class App extends Application {

  private static final String TAG = App.class.getSimpleName();

  private ExerciseModel exerciseModel;

  @Override
  public void onCreate() {
    super.onCreate();

    if (Utils.isDebug()) {
      enableStrictMode();
    }

    initSingletons();
    exerciseModel = new ExerciseModel(this);
  }

  public ExerciseModel getExerciseModel() {
    return exerciseModel;
  }

  protected void initSingletons() {
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
