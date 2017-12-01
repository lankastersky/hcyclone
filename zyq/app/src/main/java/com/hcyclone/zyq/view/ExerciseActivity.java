package com.hcyclone.zyq.view;

import android.os.Bundle;

/**
 * Shows exercise.
 */
public class ExerciseActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    createFragment(ExerciseFragment.class, ExerciseFragment.TAG);
  }
}
