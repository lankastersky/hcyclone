package com.hcyclone.zyq;

/**
 * Constants for bundle parameters.
 */
public final class BundleConstants {

  public static final String EXERCISE_ID_KEY = "exerciseId";
  public static final String EXERCISE_LEVEL_KEY = "exerciseLevel";
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  public static final String DESCRIPTION_KEY = "description";
  public static final String CURRENT_ITEM_KEY = "currentItem";

  /** Used to send audio name to audio service. */
  public static final String AUDIO_NAME_KEY = "audioNameKey";

  /** Play controls can be changed from app UI and foreground notification. Use this flag to prevent
   * looping audio messages. TODO: think about better way to do it.
   */
  public static final String DO_NOT_RESEND_AUDIO_KEY = "doNotResendAudioKey";

  /** Used to update UI with when play controls are changed from foreground notification. */
  public static final String AUDIO_BROADCAST_RECEIVER_ACTION =
      "com.hcyclone.zyq.AudioBroadcastReceiver";

  private BundleConstants() {}
}
