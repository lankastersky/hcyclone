package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
      TextView descriptionTextView = view.findViewById(R.id.detailed_description);
      descriptionTextView.setText(Html.fromHtml(description));
    }

    return view;
  }
}
