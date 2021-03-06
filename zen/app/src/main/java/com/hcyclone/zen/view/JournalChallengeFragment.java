package com.hcyclone.zen.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zen.R;
import com.hcyclone.zen.model.Challenge;

/** Shows challenge from journal. */
public class JournalChallengeFragment extends ChallengeFragment {

  public static final String TAG = JournalChallengeFragment.class.getSimpleName();
  public static final String CHALLENGE_ID = "challengeId";

  public static JournalChallengeFragment newInstance(String challengeId) {
    JournalChallengeFragment fragment = new JournalChallengeFragment();
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

    View view =
        inflater.inflate(R.layout.fragment_journal_challenge, container, false);

    getActivity().setTitle(getString(R.string.fragment_challenge_journal_entry));

    setHasOptionsMenu(true);

    Challenge challenge = challengeModel.getChallenge(challengeId);

    ratingBar = view.findViewById(R.id.fragment_challenge_rating_bar);
    ratingBar.setVisibility(View.VISIBLE);
    commentsTextView = view.findViewById(R.id.fragment_challenge_comments_text_view);
    String comments = challenge.getComments();
    commentsTextView.setVisibility(!TextUtils.isEmpty(comments) ? View.VISIBLE : View.GONE);
    if (!TextUtils.isEmpty(comments)) {
      commentsTextView.setText(challenge.getComments());
    }
    showChallengeData(view);

    return view;
  }

  @Override
  protected void refreshChallengeData() {
    // Nothing to do.
  }
}
