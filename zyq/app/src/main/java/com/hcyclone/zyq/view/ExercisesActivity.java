package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;

/**
 * Shows exercises.
 */
public class ExercisesActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(ExercisesFragment.TAG) == null) {
      ExercisesFragment exercisesFragment = new ExercisesFragment();
      exercisesFragment.setArguments(getIntent().getExtras());
      fragmentTransaction
          .add(R.id.content_container, exercisesFragment, ExercisesFragment.TAG)
          .commit();
    } else {
      Log.d(ExercisesActivity.class.getSimpleName(), "Exercises Fragment already created");
    }
  }
}
