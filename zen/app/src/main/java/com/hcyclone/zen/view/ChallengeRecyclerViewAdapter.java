package com.hcyclone.zen.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.service.PreferencesService;
import com.hcyclone.zen.view.ChallengeListFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Challenge} and makes a call to the
 * specified {@link ChallengeListFragment.OnListFragmentInteractionListener}.
 */
public class ChallengeRecyclerViewAdapter
    extends RecyclerView.Adapter<ChallengeRecyclerViewAdapter.ViewHolder> {

  private final List<Challenge> values;
  private final List<Challenge> originalValues;
  private final OnListFragmentInteractionListener listener;
  private final Context context;

  private Set<Integer> levels;
  private Set<Float> ratings;
  private boolean showDebug;

  ChallengeRecyclerViewAdapter(List<Challenge> items,
                                      Set<Integer> levels,
                                      Set<Float> ratings,
                                      OnListFragmentInteractionListener listener,
                                      Context context) {
    values = items;
    originalValues = new ArrayList<>(values);
    this.levels = levels;
    this.ratings = ratings;
    this.listener = listener;
    this.context = context;

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (sharedPreferences.getBoolean(
        PreferencesService.PREF_KEY_SHOW_CHALLENGES, false)) {
      showDebug = true;
    }

    filter();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_challenge_summary, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.item = values.get(position);

    holder.finishedTime.setText(Utils.timeToString(holder.item.getFinishedTime()));
    if (showDebug) {
      holder.finishedTime.append(" - " + holder.item.getId());
    }

    holder.contentView.setText(values.get(position).getContent());
    holder.detailsView.setText(values.get(position).getDetails());
    holder.levelView.setText(String.format(
        context.getString(R.string.fragment_challenge_level),
        Utils.localizedChallengeLevel(holder.item.getLevel(), context)));
    holder.ratingBar.setRating(holder.item.getRating());


    holder.view.setOnClickListener(v -> {
      if (listener != null) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        listener.onListFragmentInteraction(holder.item);
      }
    });
  }

  @Override
  public int getItemCount() {
    return values.size();
  }

  void filterByLevels(Set<Integer> levels) {
    this.levels = levels;
    filter();
    notifyDataSetChanged();
  }

  void filterByRating(Set<Float> ratings) {
    this.ratings = ratings;
    filter();
    notifyDataSetChanged();
  }

  private void filter() {
    values.clear();
    for (Challenge challenge : originalValues) {
      if (levels.contains(challenge.getLevel()) && ratings.contains(challenge.getRating())) {
        values.add(challenge);
      }
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    final View view;
    final TextView contentView;
    final TextView detailsView;
    final TextView finishedTime;
    final TextView levelView;
    final RatingBar ratingBar;
    Challenge item;

    ViewHolder(View view) {
      super(view);
      this.view = view;
      finishedTime = view.findViewById(R.id.finishedTime);
      contentView = view.findViewById(R.id.fragment_challenge_summary_content);
      detailsView = view.findViewById(R.id.fragment_challenge_summary_details);
      levelView = view.findViewById(R.id.fragment_challenge_summary_level);
      ratingBar = view.findViewById(R.id.fragment_challenge_summary_rating_bar);
    }
  }
}
