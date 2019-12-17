package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.crashlytics.android.Crashlytics;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;

/** Base list activity. */
public abstract class ScrolledActivity extends AdsActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scrolled);
    setupActionBar();
    showAds();
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

  protected void createFragment(Class<? extends Fragment> clazz, String tag) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(tag) == null) {
      try {
        Fragment fragment = clazz.newInstance();
        fragment.setArguments(getIntent().getExtras());
        fragmentTransaction
            .add(R.id.content_container, fragment, tag)
            .commit();
      } catch (IllegalAccessException | InstantiationException e) {
        Crashlytics.logException(e);
        Log.e(getClass().getSimpleName(), "Failed to create fragment", e);
      }
    } else {
      Log.d(getClass().getSimpleName(), "Fragment already created: " + tag);
    }
  }
}
