package com.hcyclone.zyq.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.collect.Iterables;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseModel;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.io.IOException;
import java.util.Map;

/**
 * Base fragment for the exercise.
 */
public class ExerciseFragment extends Fragment implements Step {

  public static final String TAG = ExerciseFragment.class.getSimpleName();

  private Exercise exercise;
  private AudioPlayer audioPlayer;
  private ExerciseModel exerciseModel;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_exercise, container, false);

    setHasOptionsMenu(true);

    if (getArguments() != null) {
      String exerciseId = getArguments().getString(BundleConstants.EXERCISE_ID_KEY);
      App app = (App) getContext().getApplicationContext();
      exerciseModel = app.getExerciseModel();
      exercise = exerciseModel.getExercise(exerciseId);
    }

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    audioPlayer = new AudioPlayer();
    refreshUi(getView());
  }

  @Override
  public void onStop() {
    super.onStop();
    audioPlayer.reset();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(
        exercise.type == Exercise.ExerciseType.MEDITATION
            ? R.menu.meditation_menu
            : R.menu.exercise_menu,
        menu);
    if (TextUtils.isEmpty(exerciseModel.getDescription(exercise.level, exercise.type))) {
      MenuItem item = menu.findItem(R.id.action_description);
      item.setVisible(false);
    }
    super.onCreateOptionsMenu(menu,inflater);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_description:
        Utils.showDescription(
            exerciseModel.getDescription(exercise.level, exercise.type), getContext());
        return true;
      case R.id.action_timer:
        Utils.startTimer(exercise.name, getContext());
        return true;
      case R.id.action_player:
        if (audioPlayer.isPlaying()) {
          audioPlayer.reset();
          item.setIcon(R.mipmap.ic_play_arrow_white_24dp);
        } else {
          if (!Utils.isInternetConnected(getContext())) {
            Toast
                .makeText(getContext(), "No internet connection. Try later.", Toast.LENGTH_LONG)
                .show();
            return true;
          }
          selectAudio(new OnAudioClickListener() {
            @Override
            public void play(String audioName) {
              item.setIcon(R.mipmap.ic_stop_white_24dp);
            }
          });
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // Step

  @Override
  public VerificationError verifyStep() {
    //return null if the user can go to the next step, create a new VerificationError instance otherwise
    return null;
  }

  @Override
  public void onSelected() {
    refreshUi(getView());
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

  private void selectAudio(final OnAudioClickListener onAudioClickListener) {
    final Map<String, String> audioToUriMap = AudioPlayer.AUDIO_TO_URI_MAP;
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle("Pick an audio");
    builder.setItems(
        audioToUriMap.keySet().toArray(new String[audioToUriMap.size()]),
        new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String uri = Iterables.get(audioToUriMap.values(), which);
        try {
          audioPlayer.play(uri);
        } catch (IOException e) {
          Log.e(TAG, "Failed to play audio", e);
          Toast
              .makeText(getContext(), "Failed to play audio", Toast.LENGTH_LONG)
              .show();
          return;
        }
        String audioName = Iterables.get(audioToUriMap.keySet(), which);
        onAudioClickListener.play(audioName);
      }
    });
    builder.show();
  }

  private interface OnAudioClickListener {
    void play(String audioName);
  }

  private void refreshUi(View view) {
    getActivity().setTitle(exercise.name);
    TextView descriptionTextView = view.findViewById(R.id.exercise_description);
    descriptionTextView.setText(exercise.description);
    ImageView imageView = view.findViewById(R.id.exercise_image_view);
    String fileName = exercise.imageName.substring(0, exercise.imageName.length() - 4); // without .gif
    int resID = getResources().getIdentifier(
        fileName, "mipmap", getContext().getPackageName());
    Glide.with(getActivity()).load(resID).into(imageView);
  }
}
