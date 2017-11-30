package com.hcyclone.zyq;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

    String exerciseId = getArguments().getString(BundleConstants.EXERCISE_ID_KEY);
    App app = (App) getContext().getApplicationContext();
    ExerciseModel exerciseModel = app.getExerciseModel();
    exercise = exerciseModel.getExercise(exerciseId);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    refreshUi(getView());
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
    descriptionTextView.setText(exercise.description);
    ImageView imageView = view.findViewById(R.id.exercise_image_view);
    String fileName = exercise.imageName.substring(0, exercise.imageName.length() - 4); // without .gif
    int resID = getResources().getIdentifier(
        fileName, "mipmap", getContext().getPackageName());
    //Glide.with(getActivity()).load(resID).into(imageView);
  }
}
