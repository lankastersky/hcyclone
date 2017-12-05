package com.hcyclone.zyq.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.service.AudioService;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;

import java.util.Collection;

/**
 * Plays Audio.
 */
public class AudioFragment extends ListFragment implements OnItemSelectListener<String> {

  enum ControlsState {
    STOP,
    PLAY,
    PAUSE
  }

  private static final String TAG = AudioFragment.class.getSimpleName();

  private String currentAudioName;
  private ControlsState controlsState;
  private AudioBroadcastReceiver audioBroadcastReceiver;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_audio, container, false);
    getActivity().setTitle(getString(R.string.fragment_audio_title));

    setHasOptionsMenu(true);

    recyclerView = view.findViewById(R.id.audio_recycler_view);
    Collection<String> items = buildListItems();
    currentAudioName = Iterables.get(items, 0);
    RecyclerView.Adapter adapter = new AudioRecyclerViewAdapter(items, this);
    createListLayout(recyclerView, adapter);

    audioBroadcastReceiver = new AudioBroadcastReceiver();

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    controlsState = calcControlState();

    IntentFilter filter = new IntentFilter(BundleConstants.AUDIO_BROADCAST_RECEIVER_ACTION);
    getContext().registerReceiver(audioBroadcastReceiver, filter);
  }

  @Override
  public void onPause() {
    super.onPause();
    getContext().unregisterReceiver(audioBroadcastReceiver);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.audio_menu, menu);

    MenuItem playItem = menu.findItem(R.id.action_play);
    MenuItem stopItem = menu.findItem(R.id.action_stop);
    MenuItem pauseItem = menu.findItem(R.id.action_pause);
    switch (controlsState) {
      case STOP:
        playItem.setVisible(true);
        stopItem.setVisible(false);
        pauseItem.setVisible(false);
        break;
      case PLAY:
        playItem.setVisible(false);
        stopItem.setVisible(true);
        pauseItem.setVisible(true);
        break;
      case PAUSE:
        playItem.setVisible(true);
        stopItem.setVisible(true);
        pauseItem.setVisible(false);
        break;
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch(id) {
      case R.id.action_play:
        if (play()) {
          controlsState = ControlsState.PLAY;
        }
        break;
      case R.id.action_pause:
        pause();
        controlsState = ControlsState.PAUSE;
        break;
      case R.id.action_stop:
        stop();
        controlsState = ControlsState.STOP;
        break;
      case R.id.action_set_timer:
        Utils.startTimer(getContext().getString(R.string.app_name), getContext());
        break;
    }
    getActivity().invalidateOptionsMenu();
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Collection<String> buildListItems() {
    return AudioPlayer.AUDIO_TO_URI_MAP.keySet();
  }

  @Override
  public void onItemSelected(String audioName) {
    currentAudioName = audioName;
  }

  private ControlsState calcControlState() {
    App app = (App) getContext().getApplicationContext();
    AudioPlayer player = app.getPlayer();
    if (!player.isInitied()) {
      return ControlsState.STOP;
    }
    if (player.isPlaying()) {
      return ControlsState.PLAY;
    }
    return  ControlsState.PAUSE;
  }

  private boolean play() {
    if (!Utils.isInternetConnected(getContext())) {
      Log.w(TAG, "Can't play without internet");
      Toast
          .makeText(getContext(), "No internet connection. Try later.", Toast.LENGTH_LONG)
          .show();
      return false;
    }
    Intent intent = new Intent(getContext(), AudioService.class);
    intent.putExtra(BundleConstants.AUDIO_NAME_KEY, currentAudioName);
    intent.putExtra(
        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
    intent.putExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, true);
    ContextCompat.startForegroundService(getContext(), intent);
    return true;
  }

  private void pause() {
    Intent intent = new Intent(getContext(), AudioService.class);
      intent.putExtra(
          Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
    intent.putExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, true);
    ContextCompat.startForegroundService(getContext(), intent);
  }

  private void stop() {
    Intent intent = new Intent(getContext(), AudioService.class);
    intent.putExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, true);
    getContext().stopService(intent);
  }

  /**
   * Listens to audio broadcast messages.
   */
  public class AudioBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
      if (keyEvent != null) {
        int keyCode = keyEvent.getKeyCode();
        switch (keyCode) {
          case KeyEvent.KEYCODE_MEDIA_STOP:
            controlsState = ControlsState.STOP;
            break;
          case KeyEvent.KEYCODE_MEDIA_PLAY:
            controlsState = ControlsState.PLAY;
            break;
          case KeyEvent.KEYCODE_MEDIA_PAUSE:
            controlsState = ControlsState.PAUSE;
            break;
          default:
            throw new AssertionError("Wrong key event: " + keyCode);
        }
        getActivity().invalidateOptionsMenu();
      }
    }
  }
}
