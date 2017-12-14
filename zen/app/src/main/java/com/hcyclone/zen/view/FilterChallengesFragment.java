package com.hcyclone.zen.view;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hcyclone.zen.R;

/**
 * Filters challenges list.
 */
public class FilterChallengesFragment extends DialogFragment {

  public static final String TAG = FilterChallengesFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_filter_challenges, container,
        false);
//    getDialog().setTitle("DialogFragment Tutorial");


    TextView titleView = rootView.findViewById(R.id.dialog_title);
    //titleView.setText(title);
    TextView textView = rootView.findViewById(R.id.dialog_text);
    //textView.setText(text);

    Button updateButton = rootView.findViewById(R.id.dialog_button);
    updateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    return rootView;
  }
}