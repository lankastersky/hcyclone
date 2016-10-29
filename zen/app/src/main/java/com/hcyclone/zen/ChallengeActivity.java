package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ChallengeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_challenge);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    String challengeId = getIntent().getExtras().getString(ChallengeFragment.CHALLENGE_ID);
    setTitle(ChallengeModel.getInstance().getChallenge(challengeId).getContent());

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    ChallengeFragment challengeFragment = ChallengeFragment.newInstance(challengeId);
    fragmentTransaction.add(R.id.content_container, challengeFragment).commit();
  }
}
