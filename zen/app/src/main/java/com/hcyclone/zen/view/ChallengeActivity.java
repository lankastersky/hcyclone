package com.hcyclone.zen.view;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.hcyclone.zen.App;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.model.ChallengeModel;

public class ChallengeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_challenge);
    setupActionBar();

    String challengeId = getIntent().getExtras().getString(JournalChallengeFragment.CHALLENGE_ID);
    ChallengeModel challengeModel = ((App)getApplication()).getChallengeModel();
    setTitle(challengeModel.getChallenge(challengeId).getContent());

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(JournalChallengeFragment.TAG) == null) {
      JournalChallengeFragment challengeFragment = JournalChallengeFragment.newInstance(challengeId);
      fragmentTransaction.add(R.id.content_container, challengeFragment, JournalChallengeFragment.TAG)
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
