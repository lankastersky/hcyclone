package com.hcyclone.zen.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.NestedScrollView;
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
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.App;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.AlarmService;

/** Shows challenge. */
public class ChallengeFragment extends Fragment {

  public static final String TAG = ChallengeFragment.class.getCanonicalName();
  private static final String SHARE_TEXT_DELIMITER = "\n\n";

  protected String challengeId;
  protected RatingBar ratingBar;
  protected TextView commentsTextView;
  protected ChallengeModel challengeModel;

  private EditText commentsEditText;
  private Button challengeButton;
  //private boolean showFromJournal;
  private View rankDialog;
  // Keep reference to the ShareActionProvider from the menu
  private ShareActionProvider shareActionProvider;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    challengeModel = ((App) context.getApplicationContext()).getChallengeModel();
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
    commentsTextView = view.findViewById(R.id.fragment_challenge_comments_text_view);
    commentsEditText = view.findViewById(R.id.fragment_challenge_edit_text);
    // Allow scrolling.
    commentsEditText.setOnTouchListener((view1, motionEvent) -> {
      view.getParent().requestDisallowInterceptTouchEvent(true);
      return false;
    });
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

    if (TextUtils.isEmpty(challengeId)) {
      return;
    }
    inflater.inflate(R.menu.challenge_menu, menu);

    MenuItem shareItem = menu.findItem(R.id.action_menu_share);
    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
    shareActionProvider.setOnShareTargetSelectedListener(
      (ShareActionProvider source, Intent intent) -> {
      if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
        Analytics.getInstance().sendShare(intent.getStringExtra(Intent.EXTRA_SUBJECT));
      }
      return false;
    });
    setShareIntent(createShareIntent(challengeId));
  }

  protected void refreshChallengeData() {
    Challenge challenge = challengeModel.getCurrentChallenge();
    // It's possible that the fragment is recreated but the data not. Then the activity is also
    // recreated and forces the data to load. Once the data is loaded, the activity recreates the
    // fragment with the current challenge.
    if (challenge == null) {
      Log.w(TAG, "Can't refresh challenge data: challenge is null");
      // Utils.buildDialog(getString(R.string.dialog_title_something_wrong),
      //     getString(R.string.dialog_text_failed_to_load_challenges), getActivity(), null).show();
      return;
    }
    challengeId = challenge.getId();
    Log.d(TAG, "Current challenge id: " + challengeId);

    challengeModel.setChallengeShown(challengeId);
    showChallengeData(getView());
    updateRatingBar();
    updateRankDialog();
    updateComments();
    updateChallengeButton();

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
    if (!challengeModel.checkLevelUp()) {
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

  private void createChallengeButton(View rootView) {
    challengeButton = rootView.findViewById(R.id.fragment_challenge_accept_button);
    challengeButton.setOnClickListener(view -> {
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
          // Rate before setting the challenge finished.
          rate();
          challenge.setComments(commentsEditText.getText().toString());
          challengeModel.setChallengeFinished(challengeId);
          AlarmService.getInstance().stopDailyAlarm();
          showLevelUpIfNeeded();
          break;
        default:
          Log.e(TAG, "Wrong challenge status: " + status);
          break;
      }
      updateRatingBar();
      updateRankDialog();
      updateComments();
      updateChallengeButton();
    });
  }

  private void showRankView(boolean visible) {
    rankDialog.setVisibility(visible ? View.VISIBLE : View.GONE);
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
        ratingBar.setRating(0);
        break;
    }
  }

  private void updateRankDialog() {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    @Challenge.StatusType int status = challenge.getStatus();
    switch (status) {
      case Challenge.ACCEPTED:
        boolean visible = challengeModel.isTimeToFinishChallenge();
        showRankView(visible);
        break;
      default:
        showRankView(false);
        break;
    }
  }

  private void updateComments() {
    Challenge challenge = challengeModel.getChallenge(challengeId);
    @Challenge.StatusType int status = challenge.getStatus();
    switch (status) {
      case Challenge.ACCEPTED:
        boolean timeToFinish = challengeModel.isTimeToFinishChallenge();
        commentsEditText.setVisibility(timeToFinish ? View.VISIBLE : View.GONE);
        commentsTextView.setVisibility(View.GONE);
        String comments = challenge.getComments();
        if (timeToFinish && !TextUtils.isEmpty(comments)) {
          // Show previous comments for the current challenge if any.
          commentsEditText.setText(comments);
          Analytics.getInstance().sendChallengeComments(challenge);
        }
        break;
      case Challenge.FINISHED:
        commentsEditText.setVisibility(View.GONE);
        commentsTextView.setVisibility(View.VISIBLE);
        commentsTextView.setText(challenge.getComments());
        break;
      default:
        commentsEditText.setVisibility(View.GONE);
        commentsTextView.setVisibility(View.GONE);
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
