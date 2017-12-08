package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
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
 * Shows exercise workflow.
 */
public class ExerciseFlowActivity
    extends AppCompatActivity implements StepperLayout.StepperListener {

  public static final String TAG = DescriptionFragment.class.getSimpleName();

  private StepperLayout stepperLayout;
  private Exercise exercise;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exercise_flow);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    if (getIntent().getExtras() != null) {
      String exerciseId = getIntent().getExtras().getString(BundleConstants.EXERCISE_ID_KEY);
      App app = (App) getApplicationContext();
      ExerciseModel exerciseModel = app.getExerciseModel();
      exercise = exerciseModel.getExercise(exerciseId);
      Collection<Exercise> exercises = getExercises(exercise.level, exercise.type);
      stepperLayout = findViewById(R.id.stepperLayout);
      stepperLayout
          .setAdapter(new ExerciseFlowAdapter(exercises, getSupportFragmentManager(), this));
      stepperLayout.setListener(this);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Collection<Exercise> exercises = getExercises(exercise.level, exercise.type);
    int currentStep = Iterables.indexOf(exercises, new Predicate<Exercise>() {
      @Override
      public boolean apply(Exercise input) {
        return input.getId().equals(exercise.getId());
      }
    });
    stepperLayout.setCurrentStepPosition(currentStep);
  }

  private Collection<Exercise> getExercises(Exercise.LevelType level, Exercise.ExerciseType type) {
    App app = (App) getApplicationContext();
    ExerciseModel exerciseModel = app.getExerciseModel();
    return exerciseModel.getExercises(level, type).values();
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
