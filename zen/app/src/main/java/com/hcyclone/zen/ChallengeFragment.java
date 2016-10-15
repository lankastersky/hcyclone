package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ChallengeFragment extends Fragment {

  private static final String TAG = ChallengeFragment.class.getSimpleName();
  public static final String CHALLENGE_ID = "challengeId";

  private String challengeId;
  private Button acceptButton;

  public ChallengeFragment() {
    // Required empty public constructor
  }

  public static ChallengeFragment newInstance(String challengeId) {
    ChallengeFragment fragment = new ChallengeFragment();
    Bundle args = new Bundle();
    args.putString(CHALLENGE_ID, challengeId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      challengeId = getArguments().getString(CHALLENGE_ID);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_challenge, container, false);
    if (TextUtils.isEmpty(challengeId)) {
      // Show current challenge.
      createChallengeButton(view);
      ChallengeModel.getInstance().updateCurrentChallenge();
    }
    return view;
  }



  private void createChallengeButton(View view) {
    final Challenge challenge = ChallengeModel.getInstance().getCurrentChallenge();
    ((TextView) view.findViewById(R.id.id)).setText(challenge.id);
    ((TextView) view.findViewById(R.id.content)).setText(challenge.content);
    ((TextView) view.findViewById(R.id.details)).setText(challenge.details);

    acceptButton = (Button) view.findViewById(R.id.accept_button);
    acceptButton.setVisibility(View.VISIBLE);
    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "acceptButton clicked");
        ChallengeModel.getInstance().updateCurrentChallenge();
        updateAcceptButton(challenge);
      }
    });
    updateAcceptButton(challenge);
  }

  private void updateAcceptButton(Challenge challenge) {
    if (challenge.getStatus() == Challenge.FINISHED
        || challenge.getStatus() == Challenge.DECLINED) {
      acceptButton.setEnabled(false);
    }
    acceptButton.setText(getAcceptButtonText(challenge));
  }

  private String getAcceptButtonText(Challenge challenge) {
    String text = "Accept";
    switch (challenge.getStatus()) {
      case Challenge.ACCEPTED:
        text = "Finish";
        break;
      case Challenge.FINISHED:
        text = "Finished";
        break;
      default:
        Log.e(TAG, "Wrong status to show on button: " + challenge.getStatus());
        break;
    }
    return text;
  }
}
