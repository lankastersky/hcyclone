package com.hcyclone.zen.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.AlarmService;

/** Shows challenge. */
public class ChallengeFragment extends Fragment {

  public static final String TAG = ChallengeFragment.class.getSimpleName();
  private static final String SHARE_TEXT_DELIMITER = "\n\n";

  protected String challengeId;
  protected RatingBar ratingBar;
  protected ChallengeModel challengeModel;

  private Button challengeButton;
  //private boolean showFromJournal;
  private View rankDialog;
  // Keep reference to the ShareActionProvider from the menu
  private ShareActionProvider shareActionProvider;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    challengeModel = ChallengeModel.getInstance();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_challenge, container, false);

    getActivity().setTitle(getString(R.string.challenge_current));

    setHasOptionsMenu(true);

    ratingBar = view.findViewById(R.id.fragment_challenge_rating_bar);
    rankDialog = view.findViewById(R.id.fragment_challenge_rank_dialog);

    createChallengeButton(view);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    refreshChallengeData();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    inflater.inflate(R.menu.challenge_menu, menu);
    // Retrieve the share menu item
    MenuItem shareItem = menu.findItem(R.id.action_menu_share);

    // Now get the ShareActionProvider from the item
    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

    setShareIntent(createShareIntent(challengeId));
  }

  protected void refreshChallengeData() {
    Challenge challenge = challengeModel.getCurrentChallenge();
    challengeId = challenge.getId();
    Log.d(TAG, "Current challenge id: " + challengeId);

    challengeModel.setChallengeShown(challengeId);
    showLevelUpIfNeeded();
    showChallengeData(getView());
    updateRatingBar();
    updateChallengeButton();

    // TODO: find better way to scroll to the top.
    NestedScrollView scrollView = getActivity().getWindow().getDecorView()
        .findViewById(R.id.nested_scroll_view);
    if (scrollView != null) {
      scrollView.fullScroll(View.FOCUS_UP);
    }
  }

  protected Intent createShareIntent(String challengeId) {
    Challenge challenge = challengeModel.getChallenge(challengeId);

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");

    StringBuilder textBuilder = new StringBuilder();
    textBuilder.append(challenge.getDetails());
    textBuilder.append(SHARE_TEXT_DELIMITER);
    textBuilder.append(challenge.getQuote());
    textBuilder.append(SHARE_TEXT_DELIMITER);
    textBuilder.append(String.format(
        getString(R.string.fragment_challenge_type), challenge.getType()));
    textBuilder.append(SHARE_TEXT_DELIMITER);
    textBuilder.append(String.format(getString(R.string.fragment_challenge_level),
        Utils.localizedChallengeLevel(challenge.getLevel(), getContext())));
    String text = textBuilder.toString();

    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, challenge.getContent());
    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    return shareIntent;
  }

  protected void setShareIntent(Intent shareIntent) {
    if (shareActionProvider != null) {
      shareActionProvider.setShareIntent(shareIntent);
    }
  }

  private void showLevelUpIfNeeded() {
    if (!challengeModel.checkLevelUp(challengeId)) {
      return;
    }
    int level = challengeModel.getLevel();
    Log.i(TAG, "LevelUp: " + level);
    Utils.buildDialog(
        getString(R.string.dialog_title_next_level_available),
        String.format(getString(R.string.dialog_text_next_level_available),
            Utils.localizedChallengeLevel(level, getContext())),
        getContext(),
        null).show();

    Analytics.getInstance().sendLevelUp(level);
  }

  protected void showChallengeData(View view) {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    ((TextView) view.findViewById(R.id.fragment_challenge_content)).setText(challenge.getContent());
    ((TextView) view.findViewById(R.id.fragment_challenge_details)).setText(challenge.getDetails());
    ((TextView) view.findViewById(R.id.fragment_challenge_quote)).setText(challenge.getQuote());
    if (!TextUtils.isEmpty(challenge.getQuote())) {
      view.findViewById(R.id.fragment_challenge_quote).setVisibility(View.VISIBLE);
    } else {
      view.findViewById(R.id.fragment_challenge_quote).setVisibility(View.GONE);
    }

    if (!TextUtils.isEmpty(challenge.getSourceAsHtml())) {
      ((TextView) view.findViewById(R.id.fragment_challenge_source)).setText(
          Html.fromHtml(challenge.getSourceAsHtml()));
      ((TextView) view.findViewById(R.id.fragment_challenge_source)).setMovementMethod(
          LinkMovementMethod.getInstance());
      view.findViewById(R.id.fragment_challenge_source).setVisibility(View.VISIBLE);
    } else {
      view.findViewById(R.id.fragment_challenge_source).setVisibility(View.GONE);
    }

    ((TextView) view.findViewById(R.id.fragment_challenge_type)).setText(String.format(
        getString(R.string.fragment_challenge_type), challenge.getType()));
    ((TextView) view.findViewById(R.id.fragment_challenge_level)).setText(String.format(
        getString(R.string.fragment_challenge_level),
        Utils.localizedChallengeLevel(challenge.getLevel(), getContext())));

    ratingBar.setRating(challenge.getRating());
  }

  private void createChallengeButton(View view) {
    challengeButton = view.findViewById(R.id.fragment_challenge_accept_button);
    challengeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Challenge currentChallenge = challengeModel.getCurrentChallenge();
        if (!challengeId.equals(currentChallenge.getId())) {
          Log.w(TAG, "Current challenge changed: " + currentChallenge.getId());
          refreshChallengeData();
          return;
        }
        Challenge challenge = challengeModel.getChallenge(challengeId);
        @Challenge.StatusType int status = challenge.getStatus();
        switch (status) {
          case Challenge.SHOWN:
            challengeModel.setChallengeAccepted(challengeId);
            AlarmService.getInstance().setDailyAlarm();
            break;
          case Challenge.ACCEPTED:
            rate();
            // Rate before setting the challenge finished.
            challengeModel.setChallengeFinished(challengeId);
            AlarmService.getInstance().stopDailyAlarm();
            rankDialog.setVisibility(View.GONE);
            break;
          default:
            Log.e(TAG, "Wrong challenge status: " + status);
            break;
        }
        updateChallengeButton();
        updateRatingBar();
      }
    });
  }

  private void showRankView() {
    rankDialog.setVisibility(View.VISIBLE);
    RatingBar ratingBar = rankDialog.findViewById(R.id.rank_dialog_ratingbar);
    ratingBar.setRating(0);
  }

  private void rate() {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    RatingBar ratingBar = rankDialog.findViewById(R.id.rank_dialog_ratingbar);
    challenge.setRating(ratingBar.getRating());
    Analytics.getInstance().sendChallengeRating(challenge);
  }

  private void updateRatingBar() {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    @Challenge.StatusType int status = challenge.getStatus();
    switch (status) {
      case Challenge.FINISHED:
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setRating(challenge.getRating());
        break;
      default:
        ratingBar.setVisibility(View.GONE);
        rankDialog.setVisibility(View.GONE);
        break;
    }
  }

  private void updateChallengeButton() {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    @Challenge.StatusType int status = challenge.getStatus();
    switch (status) {
      case Challenge.SHOWN:
        challengeButton.setVisibility(View.VISIBLE);
        if (challengeModel.isTimeToAcceptChallenge()) {
          challengeButton.setEnabled(true);
          challengeButton.setBackgroundColor(ContextCompat.getColor(
              getActivity(), R.color.colorPrimaryDark));
          challengeButton.setText(getString(R.string.fragment_challenge_accept));
        } else {
          challengeButton.setEnabled(false);
          challengeButton.setBackgroundColor(ContextCompat.getColor(
              getActivity(), R.color.colorPrimaryDisabled));
          challengeButton.setText(getString(
              R.string.fragment_challenge_can_start_task_before_6p_only));
        }
        break;
      case Challenge.ACCEPTED:
        boolean enabled = challengeModel.isTimeToFinishChallenge();
        challengeButton.setEnabled(enabled);
        challengeButton.setVisibility(View.VISIBLE);
        challengeButton.setBackgroundColor(enabled
            ? ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
            : ContextCompat.getColor(getActivity(), R.color.colorPrimaryDisabled));
        challengeButton.setText(enabled
            ? getString(R.string.fragment_challenge_finish)
            : getString(R.string.fragment_challenge_return_after_6pm));
        if (enabled) {
          showRankView();
        }
        break;
      case Challenge.FINISHED:
      case Challenge.DECLINED:
        challengeButton.setVisibility(View.GONE);
        break;
      default:
        Log.e(TAG, "Wrong status to show on button: " + status);
        break;
    }
  }
}
