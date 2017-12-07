package com.hcyclone.zyq.view;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
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
import com.hcyclone.zyq.AudioPlayer;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseModel;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.Locale;

/**
 * Base fragment for the exercise.
 */
public class ExerciseFragment extends Fragment implements Step {

  public static final String TAG = ExerciseFragment.class.getSimpleName();
  private static final String IMAGE_FILENAME_PREFIX_TEMPLATE = "ex_%d_%d_%s";

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
    if (TextUtils.isEmpty(exerciseModel.getPracticeDescription(exercise, getContext()))) {
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
            exerciseModel.getPracticeDescription(exercise, getContext()), getContext());
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

  private void refreshUi(View view) {
    getActivity().setTitle(exercise.name);
    TextView descriptionTextView = view.findViewById(R.id.exercise_description);
    descriptionTextView.setText(Utils.fromHtml(exercise.description));
    refreshExerciseImage(view);
  }

  private void refreshExerciseImage(View view) {
    if (!TextUtils.isEmpty(exercise.imageName)) {
      String fileName = String.format(
          Locale.ENGLISH,
          IMAGE_FILENAME_PREFIX_TEMPLATE,
          exercise.level.getValue(),
          exercise.type.getValue(),
          exercise.imageName).toLowerCase();
      // Get filename without extension.
      String fileNameWithoutExt = fileName.substring(0, fileName.length() - 4);
      int resId = getResources().getIdentifier(
          fileNameWithoutExt, "mipmap", getContext().getPackageName());
      if (resId != 0) {
        ImageView imageView = view.findViewById(R.id.exercise_image_view);
        if (fileName.endsWith(".gif")) {
          Glide.with(getActivity()).load(resId).into(imageView);
        } else {
          imageView.setImageDrawable(getResources().getDrawable(resId));
        }
      } else {
        Log.w(TAG, "Failed to load image: " + fileName);
      }
    }
  }
}
