package com.hcyclone.zyq.view.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.google.common.collect.Iterables;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.view.ExerciseFragment;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

import java.util.Collection;

/**
 * Show exercises flow.
 */
public class ExerciseFlowAdapter extends AbstractFragmentStepAdapter {

  private final Collection<Exercise> exercises;

  public ExerciseFlowAdapter(Collection<Exercise> exercises, FragmentManager fm, Context context) {
    super(fm, context);
    this.exercises = exercises;
  }

  @Override
  public Step createStep(int position) {
    ExerciseFragment exerciseFragment = new ExerciseFragment();
    Bundle bundle = new Bundle();
    Exercise exercise = Iterables.get(exercises, position);
    bundle.putString(BundleConstants.EXERCISE_ID_KEY, exercise.getId());
    exerciseFragment.setArguments(bundle);
    return exerciseFragment;
  }

  @Override
  public int getCount() {
    return exercises.size();
  }
}
