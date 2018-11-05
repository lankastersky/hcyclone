package com.hcyclone.zen.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hcyclone.zen.R;

/** Shows privacy policy. */
public final class PrivacyPolicyFragment extends Fragment {
  public static final String TAG = PrivacyPolicyFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    getActivity().setTitle(getString(R.string.pref_privacy_policy));

      WebView descriptionView = view.findViewById(R.id.description_view);
      descriptionView.setBackgroundColor(Color.TRANSPARENT);
      String filename = "file:///android_asset/privacy_policy.html";
      descriptionView.loadUrl(filename);

      return view;
  }
}
