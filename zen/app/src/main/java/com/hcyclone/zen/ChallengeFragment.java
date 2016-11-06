package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
    showChallenge(view);
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    updateChallengeIfNeeded(getView());
  }

  private void showChallenge(View view) {
    if (TextUtils.isEmpty(challengeId)) {
      // Show current challenge.
      getActivity().setTitle(getString(R.string.fragment_challenge_current));

      challenge = ChallengeModel.getInstance().getCurrentChallenge();
      createChallengeButton(view);
      ChallengeModel.getInstance().setCurrentChallengeShown();
    } else {
      // show finished challenge.
      getActivity().setTitle(getString(R.string.fragment_challenge_journal_entry));

      challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    }

    showChallengeData(view);
  }

  private void showChallengeData(View view) {
    ((TextView) view.findViewById(R.id.content)).setText(challenge.getContent());
    ((TextView) view.findViewById(R.id.details)).setText(challenge.getDetails());
    if (!TextUtils.isEmpty(challenge.getQuote())) {
      ((TextView) view.findViewById(R.id.quote)).setText(challenge.getQuote());
      view.findViewById(R.id.quote).setVisibility(View.VISIBLE);
    }

    if (!TextUtils.isEmpty(challenge.getSource())) {
      ((TextView) view.findViewById(R.id.source)).setText(Html.fromHtml(challenge.getSource()));
      ((TextView) view.findViewById(R.id.source)).setMovementMethod(
          LinkMovementMethod.getInstance());
      view.findViewById(R.id.source).setVisibility(View.VISIBLE);
    }
    ((TextView) view.findViewById(R.id.type)).setText(String.format(
        getString(R.string.fragment_challenge_type), localizedChallengeType(challenge.getType())));
    ((TextView) view.findViewById(R.id.level)).setText(String.format(
        getString(R.string.fragment_challenge_level),
        localizedChallengeLevel(challenge.getLevel())));
  }

  private void updateChallengeIfNeeded(View view) {
    if (!TextUtils.isEmpty(challengeId)) {
      // finished challenge shown and can't be changed.
      return;
    }
      if (challenge.getId().equals(ChallengeModel.getInstance().getCurrentChallenge().getId())) {
      // current challenge was not changed.
      return;
    }
    ChallengeModel.getInstance().setCurrentChallengeShown();
    challenge = ChallengeModel.getInstance().getCurrentChallenge();
    showChallengeData(view);
    updateChallengeButton();
  }

  private String localizedChallengeType(String type) {
    String result = "";
    switch (type) {
      case Challenge.TYPE_STOP_INTERNAL_DIALOG:
        result = getString(R.string.challenge_type_sid);
        break;
      default:
        break;
    }
    return result;
  }

  private String localizedChallengeLevel(int level) {
    String result = "";
    switch (level) {
      case Challenge.LEVEL_EASY:
        result = getString(R.string.challenge_level_easy);
        break;
      default:
        break;
    }
    return result;
  }

  private void createChallengeButton(View view) {
    challengeButton = (Button) view.findViewById(R.id.accept_button);
    challengeButton.setVisibility(View.VISIBLE);
    challengeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "challengeButton clicked");
        switch (ChallengeModel.getInstance().getCurrentChallenge().getStatus()) {
          case Challenge.SHOWN:
            ChallengeModel.getInstance().setCurrentChallengeAccepted();
            AlarmService.getInstance().setReminderAlarm();
            break;
          case Challenge.ACCEPTED:
            ChallengeModel.getInstance().setCurrentChallengeFinished();
            AlarmService.getInstance().stopReminderAlarm();
            break;
          default:
            Log.e(TAG, "Wrong challenge status: "
                + ChallengeModel.getInstance().getCurrentChallenge().getStatus());
        }
        updateChallengeButton();
      }
    });
    updateChallengeButton();
  }

  private void updateChallengeButton() {
    if (ChallengeModel.getInstance().getCurrentChallenge().getStatus() == Challenge.FINISHED
        || ChallengeModel.getInstance().getCurrentChallenge().getStatus() == Challenge.DECLINED) {
      // Todo: show comments if needed.
      challengeButton.setEnabled(false);
    } else {
      challengeButton.setEnabled(true);
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
    switch (ChallengeModel.getInstance().getCurrentChallenge().getStatus()) {
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
        Log.e(TAG, "Wrong status to show on button: "
            + ChallengeModel.getInstance().getCurrentChallenge().getStatus());
        break;
    }
    return result;
  }
}
