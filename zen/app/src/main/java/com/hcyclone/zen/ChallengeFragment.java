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

  public static final String CHALLENGE_ID = "challengeId";

  private String mChallengeId;
  private Button mAcceptButton;

  public ChallengeFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment ChallengeFragment.
   */
  // TODO: Rename and change types and number of parameters
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
      mChallengeId = getArguments().getString(CHALLENGE_ID);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_challenge, container, false);

    Challenge challenge;
    if (TextUtils.isEmpty(mChallengeId)) {
      challenge = ChallengeModel.getInstance().getCurrentChallenge();
      mChallengeId = challenge.id;
    } else {
      challenge = ChallengeModel.ITEM_MAP.get(mChallengeId);
    }

    createChallengeButton(view, challenge);

    return view;
  }

  private void createChallengeButton(View view, Challenge challenge) {
    ((TextView) view.findViewById(R.id.id)).setText(challenge.id);
    ((TextView) view.findViewById(R.id.content)).setText(challenge.content);
    ((TextView) view.findViewById(R.id.details)).setText(challenge.details);

    mAcceptButton = (Button) view.findViewById(R.id.accept_button);
    mAcceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(ChallengeFragment.class.getSimpleName(), "mAcceptButton clicked");
        Challenge challenge = ChallengeModel.ITEM_MAP.get(mChallengeId);
        challenge.updateStatus();
        updateAcceptButton(challenge);
      }
    });
    updateAcceptButton(challenge);
  }

  private void updateAcceptButton(Challenge challenge) {
    if (challenge.getStatus() == Challenge.NONACCEPTED
        || challenge.getStatus() == Challenge.ACCEPTED) {
      mAcceptButton.setVisibility(View.VISIBLE);
    } else {
      mAcceptButton.setVisibility(View.GONE);
    }
    mAcceptButton.setText(getAcceptButtonText(challenge));
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
//      case Challenge.DECLINED:
//        text = "Declined";
//        break;
      default:
        break;
    }
    return text;
  }
}
