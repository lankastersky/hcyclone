package com.hcyclone.zen.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.view.ChallengeListFragment.OnListFragmentInteractionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Challenge} and makes a call to the
 * specified {@link ChallengeListFragment.OnListFragmentInteractionListener}.
 */
public class ChallengeRecyclerViewAdapter
    extends RecyclerView.Adapter<ChallengeRecyclerViewAdapter.ViewHolder> {

  private static final DateFormat FINISHED_CHALLENGE_TIME_DATE_FORMAT =
      SimpleDateFormat.getDateInstance();

  private final List<Challenge> values;
  private final List<Challenge> originalValues;
  private final OnListFragmentInteractionListener listener;
  private final Context context;

  private Set<Integer> levels;
  private Set<Float> ratings;

  public ChallengeRecyclerViewAdapter(List<Challenge> items,
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

    setHasStableIds(true);

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

    Date date = new Date(holder.item.getFinishedTime());
    String dateString = FINISHED_CHALLENGE_TIME_DATE_FORMAT.format(date);
    holder.finishedTime.setText(dateString);

    holder.contentView.setText(values.get(position).getContent());
    holder.detailsView.setText(values.get(position).getDetails());
    holder.levelView.setText(String.format(
        context.getString(R.string.fragment_challenge_level),
        Utils.localizedChallengeLevel(holder.item.getLevel(), context)));
    holder.ratingBar.setRating(holder.item.getRating());


    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          listener.onListFragmentInteraction(holder.item);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return values.size();
  }

  public void filterByLevels(Set<Integer> levels) {
    this.levels = levels;
    filter();
    notifyDataSetChanged();
  }

  public void filterByRating(Set<Float> ratings) {
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

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public final View view;
    public final TextView contentView;
    public final TextView detailsView;
    public final TextView finishedTime;
    public final TextView levelView;
    public final RatingBar ratingBar;
    public Challenge item;

    public ViewHolder(View view) {
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
