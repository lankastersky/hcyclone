package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PracticeFragment extends Fragment {
  private static final String TAG = PracticeFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_practice, container, false);
    getActivity().setTitle(getString(R.string.fragment_practice_title));
    return view;
  }

}
