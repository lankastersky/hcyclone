package com.hcyclone.zyq.view;

import android.os.Bundle;

/**
 * Shows exercises.
 */
public class ExercisesActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    createFragment(ExercisesFragment.class, ExercisesFragment.TAG);
  }
}
