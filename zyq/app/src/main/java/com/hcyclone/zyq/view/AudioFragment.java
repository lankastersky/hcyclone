package com.hcyclone.zyq.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.service.AudioService;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;

import java.util.Map;

/**
 * Plays Audio.
 */
public class AudioFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_audio, container, false);
    getActivity().setTitle(getString(R.string.fragment_audio_title));

    Button button = view.findViewById(R.id.audio_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!Utils.isInternetConnected(getContext())) {
          Toast
              .makeText(getContext(), "No internet connection. Try later.", Toast.LENGTH_LONG)
              .show();
          return;
        }
        selectAudio(new SelectAudioListener() {
          @Override
          public void onSelect(String audioName, String audioUri) {
            Intent intent = new Intent(getContext(), AudioService.class);
            intent.putExtra(BundleConstants.AUDIO_NAME_KEY, audioName);
            ContextCompat.startForegroundService(getContext(), intent);
          }
        });
      }
    });
    return view;
  }

  private void selectAudio(final SelectAudioListener selectAudioListener) {
    final Map<String, String> audioToUriMap = AudioPlayer.AUDIO_TO_URI_MAP;
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle("Pick an audio");
    builder.setItems(
        audioToUriMap.keySet().toArray(new String[audioToUriMap.size()]),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String uri = Iterables.get(audioToUriMap.values(), which);
            String audioName = Iterables.get(audioToUriMap.keySet(), which);
            selectAudioListener.onSelect(audioName, uri);
          }
        });
    builder.show();
  }

  private interface SelectAudioListener {
    void onSelect(String audioName, String audioUri);
  }
}
