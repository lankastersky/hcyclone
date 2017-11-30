package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Shows description.
 */
public class DescriptionActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(DescriptionFragment.TAG) == null) {
      DescriptionFragment descriptionFragment = new DescriptionFragment();
      descriptionFragment.setArguments(getIntent().getExtras());
      fragmentTransaction
          .add(R.id.content_container, descriptionFragment, DescriptionFragment.TAG)
          .commit();
    } else {
      Log.d(DescriptionActivity.class.getSimpleName(), "Description Fragment already created");
    }
  }
}
