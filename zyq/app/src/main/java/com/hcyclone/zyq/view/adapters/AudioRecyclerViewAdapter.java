package com.hcyclone.zyq.view.adapters;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zyq.R;
import com.hcyclone.zyq.view.AudioFragment;
import com.hcyclone.zyq.view.OnItemSelectListener;

import java.util.Collection;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter} that can display audio list.
 */
public class AudioRecyclerViewAdapter
    extends ListAdapter<String, AudioRecyclerViewAdapter.ViewHolder> {

  private int selectedPosition = AudioFragment.NOT_SELECTED_STATE;

  public AudioRecyclerViewAdapter(
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
    int selectedColor = holder.view.getResources().getColor(R.color.colorSelected);
    holder.view.setBackgroundColor(selectedPosition != position ? Color.WHITE : selectedColor);
    holder.view.setTag(position);
    holder.view.setOnClickListener(v -> {
      notifyItemChanged(selectedPosition);
      selectedPosition = (int) v.getTag();
      notifyItemChanged(selectedPosition);
      if (listener != null) {
        listener.onItemSelected(holder.item);
      }
    });
  }

  public int getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(int position) {
    notifyItemChanged(selectedPosition);
    selectedPosition = position;
    notifyItemChanged(selectedPosition);
  }

  static class ViewHolder extends ListAdapter.ViewHolder<String> {

    final TextView nameView;

    ViewHolder(View view) {
      super(view);
      nameView = view.findViewById(R.id.practice_item_name);
    }
  }
}