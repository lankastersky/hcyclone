package com.hcyclone.zyq;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

/**
 * Warm Up adapter.
 */
public class WarmUpAdapter extends AbstractFragmentStepAdapter {

  static final String CURRENT_STEP_POSITION_KEY = "currentStepPositionKey";

  WarmUpAdapter(FragmentManager fm, Context context) {
    super(fm, context);
  }

  @Override
  public Step createStep(int position) {
    final ExerciseFragment step = new ExerciseFragment();
    Bundle b = new Bundle();
    b.putInt(CURRENT_STEP_POSITION_KEY, position);
    step.setArguments(b);
    return step;
  }

  @Override
  public int getCount() {
    return 3;
  }
}
