package com.hcyclone.zyq.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zyq.R;
import com.hcyclone.zyq.model.ExerciseGroup;
import com.hcyclone.zyq.view.OnItemSelectListener;

import java.util.Collection;

/**
 * {@link RecyclerView.Adapter} that can display practice items.
 */
public class PracticeRecyclerViewAdapter
    extends ListAdapter<ExerciseGroup, PracticeRecyclerViewAdapter.ViewHolder> {

  public PracticeRecyclerViewAdapter(
      Collection<ExerciseGroup> items, OnItemSelectListener<ExerciseGroup> listener) {
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
    holder.nameView.setText(holder.item.name);
    holder.typeView.setText(String.valueOf(holder.item.type));
  }

  static class ViewHolder extends ListAdapter.ViewHolder<ExerciseGroup> {

    final TextView nameView;
    final TextView typeView;

    ViewHolder(View view) {
      super(view);
      nameView = view.findViewById(R.id.practice_item_name);
      typeView = view.findViewById(R.id.practice_item_type);
    }
  }
}
