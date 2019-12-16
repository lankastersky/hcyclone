package com.hcyclone.zen.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;

public class HelpFragment extends Fragment {

  public static final String TAG = HelpFragment.class.getCanonicalName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_help));
    View view = inflater.inflate(R.layout.fragment_help, container, false);
    TextView versionView = view.findViewById(R.id.version);
    versionView.setText(String.format(getString(R.string.fragment_help_version),
        Utils.getVersionName(getContext())));
    return view;
  }
}
