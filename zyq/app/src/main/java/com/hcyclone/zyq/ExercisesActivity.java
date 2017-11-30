package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Shows exercises.
 */
public class ExercisesActivity extends AppCompatActivity {

  private Exercise.LevelType level;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exercises);
    setupActionBar();

    if (getIntent().getExtras() != null) {
      level = (Exercise.LevelType) getIntent().getExtras().get(BundleConstants.LEVEL_KEY);
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(ExercisesFragment.TAG) == null) {
      ExercisesFragment exercisesFragment = new ExercisesFragment();
      Bundle bundle = new Bundle();
      bundle.putSerializable(BundleConstants.LEVEL_KEY, level);
      exercisesFragment.setArguments(bundle);
      fragmentTransaction
          .add(R.id.content_container, exercisesFragment, ExercisesFragment.TAG)
          .commit();
    } else {
      Log.d(ExercisesActivity.class.getSimpleName(), "Exercises Fragment already created");
    }
  }

  private void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
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
