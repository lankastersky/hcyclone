package com.hcyclone.zen.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.model.ChallengeModel;

public class ChallengeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_challenge);
    setupActionBar();

    String challengeId = getIntent().getExtras().getString(ChallengeFragment.CHALLENGE_ID);
    setTitle(ChallengeModel.getInstance().getChallenge(challengeId).getContent());

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(ChallengeFragment.TAG) == null) {
      ChallengeFragment challengeFragment = ChallengeFragment.newInstance(challengeId);
      fragmentTransaction.add(R.id.content_container, challengeFragment, ChallengeFragment.TAG)
          .commit();
    } else {
      Log.d(ChallengeActivity.class.getSimpleName(), "Challenge Fragment is already created");
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
