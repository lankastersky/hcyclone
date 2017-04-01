package com.hcyclone.zen;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAgent extends BackupAgentHelper {

  static final String PREFS_CHALLENGES_BACKUP_KEY = "pref_challenges_backup_key";
  static final String PREFS_DEFAULT_BACKUP_KEY = "prefs_default_backup_key";

  @Override
  public void onCreate() {
    Log.d(BackupAgent.class.getSimpleName(), "Backing up");
    SharedPreferencesBackupHelper helper =
        new SharedPreferencesBackupHelper(this, ChallengeArchiver.SHARED_PREFERENCES_NAME);
    addHelper(PREFS_CHALLENGES_BACKUP_KEY, helper);

    SharedPreferencesBackupHelper helperDefault =
        new SharedPreferencesBackupHelper(this, "com.hcyclone.zen_preferences");
    addHelper(PREFS_DEFAULT_BACKUP_KEY, helperDefault);
  }
}
