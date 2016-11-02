package com.hcyclone.zen;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zen.ChallengeListFragment.OnListFragmentInteractionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Challenge} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ChallengeRecyclerViewAdapter
    extends RecyclerView.Adapter<ChallengeRecyclerViewAdapter.ViewHolder> {

  private final List<Challenge> values;
  private final OnListFragmentInteractionListener listener;

  public ChallengeRecyclerViewAdapter(List<Challenge> items,
                                      OnListFragmentInteractionListener listener) {
    values = items;
    this.listener = listener;
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

    DateFormat dateFormat = SimpleDateFormat.getDateInstance();
    Date date = new Date(holder.item.getFinishedTime());
    String dateString = dateFormat.format(date);
    holder.finishedTime.setText(dateString);

    holder.contentView.setText(values.get(position).getContent());
    holder.detailsView.setText(values.get(position).getDetails());

    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != listener) {
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

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View view;
    public final TextView contentView;
    public final TextView detailsView;
    public final TextView finishedTime;
    public Challenge item;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      finishedTime = (TextView) view.findViewById(R.id.finishedTime);
      contentView = (TextView) view.findViewById(R.id.content);
      detailsView = (TextView) view.findViewById(R.id.details);
    }
  }
}
