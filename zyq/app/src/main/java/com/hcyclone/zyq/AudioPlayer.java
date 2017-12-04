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

  public String getCurlurrentAudioName() {
    return currentAudioName;
  }

  public boolean isPlaying() {
    if (mediaPlayer == null) {
      return false;
    }
    return mediaPlayer.isPlaying();
  }

  public void play(String audioName, MediaPlayer.OnCompletionListener listener) throws IOException {
    currentAudioName = audioName;
    String uri = AUDIO_TO_URI_MAP.get(audioName);
    if (TextUtils.isEmpty(uri)) {
      throw new AssertionError("No audio with name " + audioName);
    }
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setDataSource(uri);
    mediaPlayer.setLooping(true);
    mediaPlayer.prepare();
    mediaPlayer.start();
    mediaPlayer.setOnCompletionListener(listener);
  }

  public void pause() {
    if (isPlaying()) {
      currentPosition = mediaPlayer.getCurrentPosition();
      mediaPlayer.pause();
    } else {
      mediaPlayer.seekTo(currentPosition);
      mediaPlayer.start();
    }
  }
  public void reset() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      currentPosition = 0;
      currentAudioName = null;
    }
  }
}
