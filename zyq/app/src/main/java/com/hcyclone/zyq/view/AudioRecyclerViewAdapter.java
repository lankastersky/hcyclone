package com.hcyclone.zyq.view;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zyq.R;

import java.util.Collection;

/**
 * {@link RecyclerView.Adapter} that can display audio list.
 */
class AudioRecyclerViewAdapter
    extends ListAdapter<String, AudioRecyclerViewAdapter.ViewHolder> {

  private int selectedPosition = 0;

  AudioRecyclerViewAdapter(
      Collection<String> items, OnItemSelectListener<String> listener) {
    super(items, listener);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cardview_practice, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);

    holder.nameView.setText(holder.item);
    holder.view.setBackgroundColor(selectedPosition != position ? Color.WHITE : Color.GRAY);
    holder.view.setTag(position);
    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        notifyItemChanged(selectedPosition);
        selectedPosition = (int) v.getTag();
        notifyItemChanged(selectedPosition);
        if (listener != null) {
          listener.onItemSelected(holder.item);
        }
      }
    });
  }

  static class ViewHolder extends ListAdapter.ViewHolder<String> {

    final TextView nameView;

    ViewHolder(View view) {
      super(view);
      nameView = view.findViewById(R.id.practice_item_name);
    }
  }
}