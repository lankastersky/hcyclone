package com.hcyclone.zyq.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.common.collect.Iterables;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.service.AudioService;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.view.adapters.AudioRecyclerViewAdapter;

import java.util.Collection;

/**
 * Plays Audio.
 */
public class AudioFragment extends ListFragment implements OnItemSelectListener<String> {

  private enum ControlsState {
    NOT_SELECTED,
    STOPPED,
    PLAYING,
    PAUSED
  }

  public static final String TAG = AudioFragment.class.getSimpleName();
  public static final int NOT_SELECTED_STATE = -1;

  private String currentAudioName;
  private ControlsState controlsState;
  private AudioBroadcastReceiver audioBroadcastReceiver;
  private ProgressBar progressBar;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_audio, container, false);
    ((AppCompatActivity) getActivity()).getSupportActionBar()
        .setTitle(getString(R.string.fragment_audio_title));

    setHasOptionsMenu(true);

    Collection<String> items = buildListItems();
    int selectedPosition = PreferenceManager.getDefaultSharedPreferences(getContext())
        .getInt(BundleConstants.CURRENT_ITEM_KEY, NOT_SELECTED_STATE);
    if (selectedPosition != NOT_SELECTED_STATE) {
      currentAudioName = Iterables.get(items, selectedPosition);
    }

    recyclerView = view.findViewById(R.id.audio_recycler_view);
    AudioRecyclerViewAdapter adapter = new AudioRecyclerViewAdapter(items, this);
    createListLayout(recyclerView, adapter);
    adapter.setSelectedPosition(selectedPosition);

    progressBar = view.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);

    audioBroadcastReceiver = new AudioBroadcastReceiver();

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    controlsState = calcControlState();

    IntentFilter filter = new IntentFilter(BundleConstants.AUDIO_BROADCAST_RECEIVER_ACTION);
    filter.addAction(AudioService.ACTION_PREPARED_AUDIO);
    getContext().registerReceiver(audioBroadcastReceiver, filter);
  }

  @Override
  public void onPause() {
    super.onPause();
    getContext().unregisterReceiver(audioBroadcastReceiver);
    AudioRecyclerViewAdapter adapter = (AudioRecyclerViewAdapter) recyclerView.getAdapter();
    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
        .putInt(BundleConstants.CURRENT_ITEM_KEY, adapter.getSelectedPosition()).apply();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.audio_menu, menu);

    MenuItem playItem = menu.findItem(R.id.action_play);
    MenuItem stopItem = menu.findItem(R.id.action_stop);
    MenuItem pauseItem = menu.findItem(R.id.action_pause);
    switch (controlsState) {
      case NOT_SELECTED:
        playItem.setVisible(false);
        stopItem.setVisible(false);
        pauseItem.setVisible(false);
        break;
      case STOPPED:
        playItem.setVisible(true);
        stopItem.setVisible(false);
        pauseItem.setVisible(false);
        break;
      case PLAYING:
        playItem.setVisible(false);
        stopItem.setVisible(true);
        pauseItem.setVisible(true);
        break;
      case PAUSED:
        playItem.setVisible(true);
        stopItem.setVisible(true);
        pauseItem.setVisible(false);
        break;
      default:
        throw new AssertionError("Not allowed state: " + controlsState);
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch(id) {
      case R.id.action_play:
        if (play()) {
          controlsState = ControlsState.PLAYING;
        }
        break;
      case R.id.action_pause:
        pause();
        controlsState = ControlsState.PAUSED;
        break;
      case R.id.action_stop:
        stop();
        controlsState = ControlsState.STOPPED;
        break;
      case R.id.action_set_timer:
        Utils.startTimer(getContext().getString(R.string.app_name), getContext());
        break;
      default:
        throw new AssertionError("No icon handler: " + id);
    }
    getActivity().invalidateOptionsMenu();
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Collection<String> buildListItems() {
    return AudioPlayer.buildAudioToUriMap(getContext()).keySet();
  }

  @Override
  public void onItemSelected(String audioName) {
    if (audioName.equals(currentAudioName)) {
      return;
    }
    currentAudioName = audioName;
    controlsState = calcControlState();
    getActivity().invalidateOptionsMenu();
  }

  private ControlsState calcControlState() {
    App app = (App) getContext().getApplicationContext();
    AudioPlayer player = app.getPlayer();
    if (TextUtils.isEmpty(currentAudioName)) {
      return ControlsState.NOT_SELECTED;
    }
    if (!player.isInitied()) {
      return ControlsState.STOPPED;
    }
    if (player.isPlaying()) {
      return ControlsState.PLAYING;
    }
    return  ControlsState.PAUSED;
  }

  private boolean play() {
//    if (!Utils.isInternetConnected(getContext())) {
//      Log.w(TAG, "Can't play without internet");
//      Toast
//          .makeText(getContext(), getString(R.string.notif_no_internet), Toast.LENGTH_LONG)
//          .show();
//      return false;
//    }
    Intent intent = new Intent(getContext(), AudioService.class);
    intent.putExtra(BundleConstants.AUDIO_NAME_KEY, currentAudioName);
    intent.putExtra(
        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
    intent.putExtra(BundleConstants.DO_NOT_RESEND_AUDIO_KEY, true);
    ContextCompat.startForegroundService(getContext(), intent);

    progressBar.setVisibility(View.VISIBLE);

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
            controlsState = ControlsState.STOPPED;
            break;
          case KeyEvent.KEYCODE_MEDIA_PLAY:
            controlsState = ControlsState.PLAYING;
            break;
          case KeyEvent.KEYCODE_MEDIA_PAUSE:
            controlsState = ControlsState.PAUSED;
            break;
          default:
            throw new AssertionError("Wrong key event: " + keyCode);
        }
        getActivity().invalidateOptionsMenu();
        return;
      }
      String action = intent.getAction();
      if (AudioService.ACTION_PREPARED_AUDIO.equals(action)) {
        progressBar.setVisibility(View.GONE);
      }
    }
  }
}
