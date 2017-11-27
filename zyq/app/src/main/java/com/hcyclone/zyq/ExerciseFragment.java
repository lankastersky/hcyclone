package com.hcyclone.zyq;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

/**
 * Base fragment for the exercise.
 */
public class ExerciseFragment extends Fragment implements Step {

  public static final String TAG = ExerciseFragment.class.getSimpleName();

  private int step;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_exercise, container, false);

    step = getArguments().getInt(WarmUpAdapter.CURRENT_STEP_POSITION_KEY);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  // Step

  @Override
  public VerificationError verifyStep() {
    //return null if the user can go to the next step, create a new VerificationError instance otherwise
    return null;
  }

  @Override
  public void onSelected() {
    //update UI when selected
    getActivity().setTitle("Warm Up " + step);
    ImageView imageView = getView().findViewById(R.id.imageView);
    Glide.with(getActivity()).load(R.mipmap.step00warmup00).into(imageView);
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

}
