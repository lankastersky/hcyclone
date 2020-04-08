package com.hcyclone.zyq;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.common.collect.ImmutableSortedMap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/** Plays audio. */
public final class AudioPlayer {

  private MediaPlayer mediaPlayer;
  private int currentPosition;
  private String currentAudioName;
  private HttpProxyCacheServer proxy;
  private volatile boolean preparing;

  private final Map<String, String> audioToUriMap;

  public static Map<String, String> buildAudioToUriMap(Context context) {
    ImmutableSortedMap.Builder<String, String> builder
            = ImmutableSortedMap.<String, String>naturalOrder()
            .put(context.getString(R.string.song_feng_shui), "http://www.qigong.ru/music/3_Feng_Shui_60.mp3")
            .put(context.getString(R.string.song_garmonic), "http://www.qigong.ru/music/4_Garmonic_72.mp3")
            .put(context.getString(R.string.song_gong_yi), "http://www.qigong.ru/music/5_Gong_Yi_63.mp3")
            .put(context.getString(R.string.song_himalaya), "http://www.qigong.ru/music/2_Himalaya_50.mp3")
            .put(context.getString(R.string.song_tai_chi), "http://www.qigong.ru/music/8_Tai_Chi_61.mp3")
            .put(context.getString(R.string.song_tibetian), "http://www.qigong.ru/music/6_Tibetian_Bowls_66.mp3")
            .put(context.getString(R.string.song_big_tree), "http://www.qigong.ru/music/7_Big_Tree_44.mp3")
            .put(context.getString(R.string.song_yan_qi), "http://www.qigong.ru/music/1_Yan_Qi_73.mp3");
    return builder.build();
  }

  public AudioPlayer(Context context) {
    audioToUriMap = buildAudioToUriMap(context);
    proxy = new HttpProxyCacheServer.Builder(context)
        //.cacheDirectory(new File(context.getExternalCacheDir(), "video-cache"))
        .build();
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
      MediaPlayer.OnPreparedListener preparedListener,
      MediaPlayer.OnCompletionListener completionListener,
      final MediaPlayer.OnErrorListener errorListener) throws IOException {

    Analytics.getInstance().sendAudio(audioName);

    reset();

    String uri = audioToUriMap.get(audioName);
    if (TextUtils.isEmpty(uri)) {
      throw new AssertionError("No audio with name " + audioName);
    }

    currentAudioName = audioName;

    mediaPlayer = new MediaPlayer();

    String proxyUrl = proxy.getProxyUrl(uri);

    mediaPlayer.setDataSource(proxyUrl);
    mediaPlayer.setLooping(true);
    preparing = true;
    mediaPlayer.prepareAsync();
    mediaPlayer.setOnPreparedListener(mp -> {
      preparing = false;
      mediaPlayer.start();
      preparedListener.onPrepared(mp);
    });
    mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
      preparing = false;
      return errorListener.onError(mediaPlayer, i, i1);
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
      mediaPlayer.reset();
      currentPosition = 0;
      currentAudioName = null;
      preparing = false;
    }
  }
}
