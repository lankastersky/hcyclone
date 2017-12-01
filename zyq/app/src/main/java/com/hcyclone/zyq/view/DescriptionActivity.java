package com.hcyclone.zyq.view;

import android.os.Bundle;

/**
 * Shows description.
 */
public class DescriptionActivity extends ScrolledActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    createFragment(DescriptionFragment.class, DescriptionFragment.TAG);
  }
}
