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
  private Challenge challenge;
  private Button challengeButton;

  public ChallengeFragment() {
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
      challenge = ChallengeModel.getInstance().getCurrentChallenge();
      createChallengeButton(view);
      ChallengeModel.getInstance().setCurrentChallengeShown();
    } else {
      // show finished challenge.
      challenge = ChallengeModel.getInstance().getChallenge(challengeId);

      TextView statusView = (TextView) view.findViewById(R.id.status);
      statusView.setText(String.format(getString(R.string.fragment_challenge_status),
          challengeStatusAsString(challenge.getStatus())));
      statusView.setVisibility(View.VISIBLE);
    }

    getActivity().setTitle(challenge.getContent());

    ((TextView) view.findViewById(R.id.content)).setText(challenge.getContent());
    ((TextView) view.findViewById(R.id.details)).setText(challenge.getDetails());

    return view;
  }

  private void createChallengeButton(View view) {
    challengeButton = (Button) view.findViewById(R.id.accept_button);
    challengeButton.setVisibility(View.VISIBLE);
    challengeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "challengeButton clicked");
        switch (challenge.getStatus()) {
          case Challenge.SHOWN:
            ChallengeModel.getInstance().setCurrentChallengeAccepted();
            AlarmService.getInstance().setReminderAlarm();
            break;
          case Challenge.ACCEPTED:
            ChallengeModel.getInstance().setCurrentChallengeFinished();
            AlarmService.getInstance().stopReminderAlarm();
            break;
          default:
            Log.e(TAG, "Wrong challenge status: " + challenge.getStatus());
        }
        updateChallengeButton();
      }
    });
    updateChallengeButton();
  }

  private void updateChallengeButton() {
    if (challenge.getStatus() == Challenge.FINISHED
        || challenge.getStatus() == Challenge.DECLINED) {
      // Todo: show comments if needed.
      challengeButton.setEnabled(false);
    }
    challengeButton.setText(getChallengeButtonText());
  }

  private String challengeStatusAsString(int status) {
    String result = "";
    switch (status) {
      case Challenge.FINISHED:
        result = getString(R.string.fragment_challenge_finished);
        break;
      case Challenge.DECLINED:
        result = getString(R.string.fragment_challenge_declined);
        break;
      default:
        Log.d(TAG, "Wrong status to convert to string: " + status);
        break;
    }
    return result;
  }

  private String getChallengeButtonText() {
    String result = "";
    switch (challenge.getStatus()) {
      case Challenge.UNKNOWN:
      case Challenge.SHOWN:
        result = getString(R.string.fragment_challenge_accept);
        break;
      case Challenge.ACCEPTED:
        result = getString(R.string.fragment_challenge_finish);
        break;
      case Challenge.FINISHED:
        result = getString(R.string.fragment_challenge_finished);
        break;
      case Challenge.DECLINED:
        result = getString(R.string.fragment_challenge_declined);
        break;
      default:
        Log.e(TAG, "Wrong status to show on button: " + challenge.getStatus());
        break;
    }
    return result;
  }
}
