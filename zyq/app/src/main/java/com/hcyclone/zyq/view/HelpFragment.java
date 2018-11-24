package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;

import java.io.IOException;

public class HelpFragment extends Fragment {

  public static final String TAG = HelpFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    ((AppCompatActivity) getActivity()).getSupportActionBar()
        .setTitle(getString(R.string.fragment_help_title));

    View view = inflater.inflate(R.layout.fragment_help, container, false);

    TextView versionView = view.findViewById(R.id.version);
    versionView.setText(String.format(getString(R.string.fragment_help_version),
        Utils.getVersionName(getContext())));

    TextView helpTextView = view.findViewById(R.id.help_text);
    try {
      String language = Utils.getCurrentLocale(getContext()).getLanguage();
      int resourceId;
      if ("ru".equals(language)) {
        resourceId = R.raw.help_html;
      } else {
        resourceId = R.raw.help_en_html;
      }

      String helpText = Utils.readFromRawResources(resourceId, getContext());
      helpTextView.setText(Utils.fromHtml(helpText));
      ((TextView) view.findViewById(R.id.help_text)).setMovementMethod(
          LinkMovementMethod.getInstance());
    } catch (IOException e) {
      Log.w(TAG, "Failed to read text from resources", e);
    }

    return view;
  }
}
