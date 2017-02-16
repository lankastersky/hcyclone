package com.hcyclone.zen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class ChallengeFragment extends Fragment {

  private static final String TAG = ChallengeFragment.class.getSimpleName();
  public static final String CHALLENGE_ID = "challengeId";

  private String challengeId;
  private Button challengeButton;
  private boolean showFromJournal;
  private View rankDialog;
  private RatingBar ratingBar;

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
    if (!TextUtils.isEmpty(challengeId)) {
      showFromJournal = true;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_challenge, container, false);
    createUI(view);
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!showFromJournal) {
      challengeId = ChallengeModel.getInstance().getCurrentChallenge().getId();
    }
    updateUI();
  }

  private void createUI(View view) {
    ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
    rankDialog = view.findViewById(R.id.rank_dialog);
    if (!showFromJournal) {
      // Show current challenge.
      getActivity().setTitle(getString(R.string.fragment_challenge_current));
      createChallengeButton(view);
    } else {
      // show finished challenge.
      getActivity().setTitle(getString(R.string.fragment_challenge_journal_entry));
    }
  }

  private void updateUI() {
    ChallengeModel.getInstance().setCurrentChallengeShown();
    showChallengeData();
    updateRatingBar();
    if (!showFromJournal) {
      updateChallengeButton();
    }
  }

  private void showChallengeData() {
    View view = getView();
    Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    ((TextView) view.findViewById(R.id.content)).setText(challenge.getContent());
    ((TextView) view.findViewById(R.id.details)).setText(challenge.getDetails());
    ((TextView) view.findViewById(R.id.quote)).setText(challenge.getQuote());
    if (!TextUtils.isEmpty(challenge.getQuote())) {
      view.findViewById(R.id.quote).setVisibility(View.VISIBLE);
    } else {
      view.findViewById(R.id.quote).setVisibility(View.GONE);
    }

    if (!TextUtils.isEmpty(challenge.getSourceAsHtml())) {
      ((TextView) view.findViewById(R.id.source)).setText(
          Html.fromHtml(challenge.getSourceAsHtml()));
      ((TextView) view.findViewById(R.id.source)).setMovementMethod(
          LinkMovementMethod.getInstance());
      view.findViewById(R.id.source).setVisibility(View.VISIBLE);
    } else {
      view.findViewById(R.id.source).setVisibility(View.GONE);
    }

    ((TextView) view.findViewById(R.id.type)).setText(String.format(
        getString(R.string.fragment_challenge_type), challenge.getType()));
    ((TextView) view.findViewById(R.id.level)).setText(String.format(
        getString(R.string.fragment_challenge_level),
        localizedChallengeLevel(challenge.getLevel())));

    ratingBar.setRating(challenge.getRating());
  }

  private String localizedChallengeLevel(int level) {
    String result = "";
    switch (level) {
      case Challenge.LEVEL_LOW:
        result = getString(R.string.challenge_level_low);
        break;
      case Challenge.LEVEL_MEDIUM:
        result = getString(R.string.challenge_level_medium);
        break;
      case Challenge.LEVEL_HIGH:
        result = getString(R.string.challenge_level_high);
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
        Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
        switch (challenge.getStatus()) {
          case Challenge.SHOWN:
            ChallengeModel.getInstance().setCurrentChallengeAccepted();
            AlarmService.getInstance().setReminderAlarm();
            break;
          case Challenge.ACCEPTED:
            ChallengeModel.getInstance().setCurrentChallengeFinished();
            AlarmService.getInstance().stopReminderAlarm();
            rankDialog.setVisibility(View.GONE);
            rate();
            break;
          default:
            Log.e(TAG, "Wrong challenge status: " + challenge.getStatus());
        }
        updateChallengeButton();
        updateRatingBar();
      }
    });
  }

  private void showRankView() {
    rankDialog.setVisibility(View.VISIBLE);
    RatingBar ratingBar = (RatingBar)rankDialog.findViewById(R.id.rank_dialog_ratingbar);
    ratingBar.setRating(0);
  }

  private void rate() {
    Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    RatingBar ratingBar = (RatingBar)rankDialog.findViewById(R.id.rank_dialog_ratingbar);
    challenge.setRating(ratingBar.getRating());
    Analytics.getInstance().sendChallengeRating(challenge);
  }

  private void updateRatingBar() {
    Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    int status = challenge.getStatus();
    switch (status) {
      case Challenge.FINISHED:
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setRating(challenge.getRating());
        break;
      default:
        ratingBar.setVisibility(View.GONE);
        break;
    }
  }

  private void updateChallengeButton() {
    Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    switch (challenge.getStatus()) {
      case Challenge.ACCEPTED:
        boolean enabled = ChallengeModel.getInstance().isTimeToFinishCurrentChallenge();
          challengeButton.setEnabled(enabled);
          challengeButton.setBackgroundColor(enabled
              ? ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
              : ContextCompat.getColor(getActivity(), R.color.colorPrimaryDisabled));
        if (enabled) {
          showRankView();
        }
        break;
      case Challenge.FINISHED:
      case Challenge.DECLINED:
        challengeButton.setVisibility(View.GONE);
        break;
      default:
        challengeButton.setEnabled(true);
        challengeButton.setVisibility(View.VISIBLE);
        challengeButton.setBackgroundColor(ContextCompat.getColor(
            getActivity(), R.color.colorPrimaryDark));
    }
    challengeButton.setText(getChallengeButtonText());
  }

  private String getChallengeButtonText() {
    String result = "";
    Challenge challenge = ChallengeModel.getInstance().getChallenge(challengeId);
    switch (challenge.getStatus()) {
      case Challenge.UNKNOWN:
      case Challenge.SHOWN:
        result = getString(R.string.fragment_challenge_accept);
        break;
      case Challenge.ACCEPTED:
          result = challengeButton.isEnabled() ? getString(R.string.fragment_challenge_finish)
              : getString(R.string.fragment_challenge_return_after_6pm);
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
