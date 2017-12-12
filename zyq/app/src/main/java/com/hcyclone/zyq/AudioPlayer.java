package com.hcyclone.zyq;

import android.media.MediaPlayer;
import android.text.TextUtils;

import com.google.common.collect.ImmutableSortedMap;

import java.io.IOException;
import java.util.Map;

/**
 * Plays audio.
 */
public final class AudioPlayer {

  public static final Map<String, String> AUDIO_TO_URI_MAP;

  private MediaPlayer mediaPlayer;
  private int currentPosition;
  private String currentAudioName;
  private volatile boolean preparing;

  static {
    ImmutableSortedMap.Builder<String, String> builder
        = ImmutableSortedMap.<String, String>naturalOrder()
        .put("Feng_Shui (60 мин.)", "http://www.qigong.ru/music/3_Feng_Shui_60.mp3")
        .put("Garmonic (72 мин.)", "http://www.qigong.ru/music/4_Garmonic_72.mp3")
        .put("Gong_Yi (63 мин.)", "http://www.qigong.ru/music/5_Gong_Yi_63.mp3")
        .put("Himalaya (50 мин.)", "http://www.qigong.ru/music/2_Himalaya_50.mp3")
        .put("Tai Chi (61 мин.)", "http://www.qigong.ru/music/8_Tai_Chi_61.mp3")
        .put("Tibetian_Bowls (66 мин.)", "http://www.qigong.ru/music/6_Tibetian_Bowls_66.mp3")
        .put("Большое дерево (45 мин.)", "http://www.qigong.ru/music/7_Big_Tree_44.mp3")
        .put("Ян-ци (73 мин.)", "http://www.qigong.ru/music/1_Yan_Qi_73.mp3");
    AUDIO_TO_URI_MAP = builder.build();
  }

  public int getCurrentPosition() {
    if (isPlaying()) {
      return mediaPlayer.getCurrentPosition();
    }
    return currentPosition;
  }

  public String getCurrentAudioName() {
    return currentAudioName;
  }

  public boolean isPlaying() {
    if (mediaPlayer == null) {
      return false;
    }
    return mediaPlayer.isPlaying() || preparing;
  }

  public boolean isInitied() {
    return !TextUtils.isEmpty(getCurrentAudioName());
  }

  public void play(
      String audioName,
      MediaPlayer.OnCompletionListener completionListener,
      final MediaPlayer.OnErrorListener errorListener) throws IOException {

    Analytics.getInstance().sendAudio(audioName);

    reset();

    String uri = AUDIO_TO_URI_MAP.get(audioName);
    if (TextUtils.isEmpty(uri)) {
      throw new AssertionError("No audio with name " + audioName);
    }

    currentAudioName = audioName;

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setDataSource(uri);
    // TODO: change loop mode in UI.
    mediaPlayer.setLooping(true);
    preparing = true;
    mediaPlayer.prepareAsync();
    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        preparing = false;
        mediaPlayer.start();
      }
    });
    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      @Override
      public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        preparing = false;
        return errorListener.onError(mediaPlayer, i, i1);
      }
    });
    mediaPlayer.setOnCompletionListener(completionListener);
  }

  public void play() {
    mediaPlayer.seekTo(currentPosition);
    mediaPlayer.start();
  }

  public void pause() {
    currentPosition = mediaPlayer.getCurrentPosition();
    mediaPlayer.pause();
  }

  public void reset() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      currentPosition = 0;
      currentAudioName = null;
      preparing = false;
    }
  }
}
