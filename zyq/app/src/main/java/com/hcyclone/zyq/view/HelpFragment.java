package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zyq.R;

public class HelpFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_help));
    return inflater.inflate(R.layout.fragment_help, container, false);
    // TODO: add version from resources.
  }
}
