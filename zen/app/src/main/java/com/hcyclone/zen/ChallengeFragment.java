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
      challenge = ChallengeModel.getInstance().getCurrentChallenge();
      createChallengeButton(view);
      ChallengeModel.getInstance().setCurrentChallengeShown();
    } else {
      challenge = ChallengeModel.getInstance().getChallengeById(challengeId);
    }

    ((TextView) view.findViewById(R.id.id)).setText(challenge.id);
    ((TextView) view.findViewById(R.id.content)).setText(challenge.content);
    ((TextView) view.findViewById(R.id.details)).setText(challenge.details);

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

  private String getChallengeButtonText() {
    String text = "";
    switch (challenge.getStatus()) {
      case Challenge.UNKNOWN:
      case Challenge.SHOWN:
        text = "Accept";
        break;
      case Challenge.ACCEPTED:
        text = "Finish";
        break;
      case Challenge.FINISHED:
        text = "Finished";
        break;
      case Challenge.DECLINED:
        text = "Declined";
      default:
        Log.e(TAG, "Wrong status to show on button: " + challenge.getStatus());
        break;
    }
    return text;
  }
}
