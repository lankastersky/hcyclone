package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseModel;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.Collection;

/**
 * Warm Up workflow.
 */
public class WarmUpActivity extends AppCompatActivity implements StepperLayout.StepperListener {

  public static final String TAG = DescriptionFragment.class.getSimpleName();

  private StepperLayout stepperLayout;
  private Exercise exercise;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_warmup);

    if (getIntent().getExtras() != null) {
      String exerciseId = getIntent().getExtras().getString(BundleConstants.EXERCISE_ID_KEY);
      App app = (App) getApplicationContext();
      ExerciseModel exerciseModel = app.getExerciseModel();
      exercise = exerciseModel.getExercise(exerciseId);
      Collection<Exercise> exercises =
          exerciseModel.getExercises(exercise.level, exercise.type).values();
      stepperLayout = findViewById(R.id.stepperLayout);
      stepperLayout
          .setAdapter(new WarmUpAdapter(exercises, getSupportFragmentManager(), this));
      stepperLayout.setListener(this);

      int currentStep = Iterables.indexOf(exercises, new Predicate<Exercise>() {
        @Override
        public boolean apply(Exercise input) {
          return input.getId().equals(exercise.getId());
        }
      });
      stepperLayout.setCurrentStepPosition(currentStep);
    }
  }

  // StepperLayout.StepperListener

  @Override
  public void onCompleted(View completeButton) {
    finish();
  }

  @Override
  public void onError(VerificationError verificationError) {
    Toast
        .makeText(
            this,
            "onError! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onStepSelected(int newStepPosition) {
  }

  @Override
  public void onReturn() {
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
