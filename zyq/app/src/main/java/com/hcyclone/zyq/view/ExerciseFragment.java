package com.hcyclone.zyq.view;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseModel;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

/**
 * Base fragment for the exercise.
 */
public class ExerciseFragment extends Fragment implements Step {

  public static final String TAG = ExerciseFragment.class.getSimpleName();

  private Exercise exercise;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_exercise, container, false);

    setHasOptionsMenu(true);

    if (getArguments() != null) {
      String exerciseId = getArguments().getString(BundleConstants.EXERCISE_ID_KEY);
      App app = (App) getContext().getApplicationContext();
      ExerciseModel exerciseModel = app.getExerciseModel();
      exercise = exerciseModel.getExercise(exerciseId);
    }

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    refreshUi(getView());
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(
        exercise.type == Exercise.ExerciseType.MEDITATION
            ? R.menu.meditation_menu
            : R.menu.exercise_menu,
        menu);
    super.onCreateOptionsMenu(menu,inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_description:
        Utils.showDescription(getContext());
        return true;
      case R.id.action_timer:
        Utils.startTimer(exercise.name, getContext());
        return true;
      case R.id.action_player:
        Uri uri = Uri.parse("http://www.qigong.ru/music/1_Yan_Qi_73.mp3");
//        Utils.playMedia(uri, getContext());
        try {
          playAudio("http://www.qigong.ru/music/1_Yan_Qi_73.mp3");
        } catch (Exception e) {
          Log.e(TAG, "Failed to play music", e);
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

  private MediaPlayer mediaPlayer;

  private void playAudio(String url) throws Exception {
    killMediaPlayer();

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setDataSource(url);
    mediaPlayer.prepare();
    mediaPlayer.start();
  }

  private void killMediaPlayer() {
    if(mediaPlayer!=null) {
      try {
        mediaPlayer.release();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
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
