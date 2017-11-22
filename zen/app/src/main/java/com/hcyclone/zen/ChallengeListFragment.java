package com.hcyclone.zen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChallengeListFragment extends Fragment {

  private OnListFragmentInteractionListener onListFragmentInteractionListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      onListFragmentInteractionListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_challenge_list));

    View view;
    List<Challenge> finishedChallenges = ChallengeModel.getInstance().getFinishedChallenges();
//    for (int i = 0; i < 9; i++) {
//      finishedChallenges.addAll(ChallengeModel.getInstance().getFinishedChallenges());
//    }
    if (finishedChallenges.isEmpty()) {
      view = inflater.inflate(R.layout.fragment_challenge_list_empty, container, false);
    } else {
      view = inflater.inflate(R.layout.fragment_challenge_list, container, false);
    }

    // Set the adapter
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      recyclerView.setAdapter(new ChallengeRecyclerViewAdapter(
          finishedChallenges, onListFragmentInteractionListener));
      recyclerView.setNestedScrollingEnabled(false);
    }
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    onListFragmentInteractionListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnListFragmentInteractionListener {
    void onListFragmentInteraction(Challenge item);
  }
}
