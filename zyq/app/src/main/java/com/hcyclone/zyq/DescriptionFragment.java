package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Shows description.
 */
public class DescriptionFragment extends Fragment {

  public static final String TAG = DescriptionFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_description, container, false);
    getActivity().setTitle(getString(R.string.fragment_practice_description_title));

    return view;
  }
}
