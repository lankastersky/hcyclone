package com.hcyclone.zen.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hcyclone.zen.R;

/** Shows Privacy policy fragment */
public final class PrivacyPolicyActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_privacy_policy);
    setupActionBar();

    Fragment fragment = getSupportFragmentManager().findFragmentByTag(
          PrivacyPolicyFragment.TAG);
      if (fragment == null) {
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.privacy_policy_content_container, new PrivacyPolicyFragment(),
                PrivacyPolicyFragment.TAG)
            .commit();
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
