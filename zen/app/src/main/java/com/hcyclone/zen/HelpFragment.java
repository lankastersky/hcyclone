package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HelpFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_help));
    View view = inflater.inflate(R.layout.fragment_help, container, false);
    TextView versionView = (TextView) view.findViewById(R.id.version);
    versionView.setText(String.format(getString(R.string.fragment_help_version),
        Utils.getInstance().getVersionName()));
    return view;
  }
}
