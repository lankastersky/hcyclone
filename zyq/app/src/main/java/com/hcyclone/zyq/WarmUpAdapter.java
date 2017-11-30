package com.hcyclone.zyq;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.google.common.collect.Iterables;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

import java.util.Collection;

/**
 * Warm Up exercises adapter.
 */
public class WarmUpAdapter extends AbstractFragmentStepAdapter {

  private final Collection<Exercise> exercises;

  WarmUpAdapter(Collection<Exercise> exercises, FragmentManager fm, Context context) {
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
