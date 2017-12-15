package com.hcyclone.zen.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zen.R;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeFilterModel;
import com.hcyclone.zen.model.ChallengeModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChallengeListFragment extends Fragment {

  // Size of @LevelType.
  final boolean[] levels = {true, true, true};
  // Size of getContext().getResources().getInteger(R.integer.stars_amount) + 1;
  final boolean[] ratings = {true, true, true, true, true};
  private OnListFragmentInteractionListener onListFragmentInteractionListener;
  private RecyclerView recyclerView;
  private ChallengeFilterModel challengeFilterModel;

  private static Set<Integer> levelsToSet(boolean[] items) {
    Set<Integer> levelsSet = new HashSet<>();
    for (int i = 0; i < items.length; i++) {
      if (items[i]) {
        @Challenge.LevelType int level = i + 1; // levels enumeration is started from 1.
        levelsSet.add(level);
      }
    }
    return levelsSet;
  }

  private static Set<Float> ratingsToSet(boolean[] items) {
    Set<Float> ratingsSet = new HashSet<>();
    for (int i = 0; i < items.length; i++) {
      if (items[i]) {
        ratingsSet.add((float) i);
      }
    }
    return ratingsSet;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      onListFragmentInteractionListener = (OnListFragmentInteractionListener) context;
      challengeFilterModel = new ChallengeFilterModel(context);
      challengeFilterModel.restoreLevels(levels);
      challengeFilterModel.restoreRatings(ratings);
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnListFragmentInteractionListener");
    }
  }

//  private void showFilterDialog() {
//    FilterChallengesFragment fragment = new FilterChallengesFragment();
//    fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.FilterDialogCustom);
//    fragment.show(getActivity().getSupportFragmentManager(), FilterChallengesFragment.TAG);
//  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_challenge_list));

    setHasOptionsMenu(true);

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
      recyclerView = (RecyclerView) view;
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      recyclerView.setAdapter(new ChallengeRecyclerViewAdapter(
          finishedChallenges,
          levelsToSet(levels),
          ratingsToSet(ratings),
          onListFragmentInteractionListener,
          getContext()));
      recyclerView.setNestedScrollingEnabled(false);
    }
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    onListFragmentInteractionListener = null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.challenge_list_menu, menu);
    List<Challenge> finishedChallenges = ChallengeModel.getInstance().getFinishedChallenges();
    if (finishedChallenges.isEmpty()) {
      MenuItem item = menu.findItem(R.id.action_filter_rate);
      item.setVisible(false);
      item = menu.findItem(R.id.action_filter_level);
      item.setVisible(false);
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_filter_rate:
        showFilterByRatingDialog();
        return true;
      case R.id.action_filter_level:
        showFilterByLevelDialog();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showFilterByLevelDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMultiChoiceItems(
        R.array.levels, levels, new DialogInterface.OnMultiChoiceClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            levels[which] = isChecked;
          }
        });
    builder.setTitle(getString(R.string.filter_challenges_by_level));
    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        challengeFilterModel.storeLevels(levels);
        ((ChallengeRecyclerViewAdapter) recyclerView.getAdapter())
            .filterByLevels(levelsToSet(levels));
      }
    });
    builder.create().show();
  }

  private void showFilterByRatingDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMultiChoiceItems(
        R.array.ratings, ratings, new DialogInterface.OnMultiChoiceClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            ratings[which] = isChecked;
          }
        });
    builder.setTitle(getString(R.string.filter_challenges_by_rating));
    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        challengeFilterModel.storeRatings(ratings);
        ((ChallengeRecyclerViewAdapter) recyclerView.getAdapter())
            .filterByRating(ratingsToSet(ratings));
      }
    });
    builder.create().show();
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
