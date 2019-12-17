package com.hcyclone.zyq.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.R;

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

    if (getArguments() != null) {
      String description = getArguments().getString(BundleConstants.DESCRIPTION_KEY);
      WebView descriptionView = view.findViewById(R.id.description_view);
      descriptionView.setBackgroundColor(Color.TRANSPARENT);
      descriptionView.loadData(description, "text/html; charset=UTF-8", null);
    }

    return view;
  }
}
