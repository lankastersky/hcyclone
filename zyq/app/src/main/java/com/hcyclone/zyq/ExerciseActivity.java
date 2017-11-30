package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Shows exercise.
 */
public class ExerciseActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(ExerciseFragment.TAG) == null) {
      ExerciseFragment exerciseFragment = new ExerciseFragment();
      exerciseFragment.setArguments(getIntent().getExtras());
      fragmentTransaction
          .add(R.id.content_container, exerciseFragment, ExerciseFragment.TAG)
          .commit();
    } else {
      Log.d(ExercisesActivity.class.getSimpleName(), "Exercise Fragment already created");
    }
  }
}
