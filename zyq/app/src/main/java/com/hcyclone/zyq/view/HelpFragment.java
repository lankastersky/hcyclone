package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zyq.R;

public class HelpFragment extends Fragment {

  public static final String TAG = HelpFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    CollapsingToolbarLayout collapsingToolbar = getActivity().findViewById(R.id.collapsing_toolbar);
    collapsingToolbar.setTitle(getString(R.string.fragment_help_title));

    return inflater.inflate(R.layout.fragment_help, container, false);
    // TODO: add version from resources.
  }
}
