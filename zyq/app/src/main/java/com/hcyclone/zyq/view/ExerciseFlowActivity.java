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
import com.hcyclone.zyq.model.Exercise.LevelType;
import com.hcyclone.zyq.model.ExerciseModel;
import com.hcyclone.zyq.view.adapters.ExerciseFlowAdapter;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exercise_flow);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      final String exerciseId = extras.getString(BundleConstants.EXERCISE_ID_KEY);
      LevelType level = (LevelType) extras.getSerializable(BundleConstants.EXERCISE_LEVEL_KEY);
      App app = (App) getApplicationContext();
      ExerciseModel exerciseModel = app.getExerciseModel();
      Exercise exercise = exerciseModel.getExercise(exerciseId);
      // It's possible that current flow level can be more than exercise level.
      Collection<Exercise> exercises = getExercises(level, exercise.type);

      stepperLayout = findViewById(R.id.stepperLayout);
      stepperLayout
          .setAdapter(new ExerciseFlowAdapter(exercises, getSupportFragmentManager(), this));
      int currentStep;
      if (savedInstanceState == null) {
        currentStep = Iterables.indexOf(exercises, new Predicate<Exercise>() {
          @Override
          public boolean apply(Exercise input) {
            return input.getId().equals(exerciseId);
          }
        });
      } else {
        currentStep = savedInstanceState.getInt(BundleConstants.CURRENT_ITEM_KEY);
      }
      stepperLayout.setCurrentStepPosition(currentStep);
      stepperLayout.setListener(this);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Refresh visible fragment this way. E.g. after showing the description activity.
    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(BundleConstants.CURRENT_ITEM_KEY, stepperLayout.getCurrentStepPosition());
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

  private Collection<Exercise> getExercises(Exercise.LevelType level, Exercise.ExerciseType type) {
    App app = (App) getApplicationContext();
    ExerciseModel exerciseModel = app.getExerciseModel();
    return exerciseModel.getExercises(level, type).values();
  }
}
