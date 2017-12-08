package com.hcyclone.zyq.view;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hcyclone.zyq.R;
import com.hcyclone.zyq.model.Exercise;

import java.util.Collection;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Exercise}.
 */
public class ExerciseRecyclerViewAdapter
    extends ListAdapter<Exercise, ExerciseRecyclerViewAdapter.ViewHolder> {

  ExerciseRecyclerViewAdapter(
      Collection<Exercise> items, OnItemSelectListener<Exercise> listener) {
    super(items, listener);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cardview_exercise_summary, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    holder.nameView.setText(holder.item.name);
    holder.tagsView.setVisibility(!TextUtils.isEmpty(holder.item.tags) ? View.VISIBLE : View.GONE);
    holder.tagsView.setText(String.valueOf(holder.item.tags));
  }

  static class ViewHolder extends ListAdapter.ViewHolder<Exercise> {

    final TextView nameView;
    final TextView tagsView;

    ViewHolder(View view) {
      super(view);
      nameView = view.findViewById(R.id.exercise_name);
      tagsView = view.findViewById(R.id.exercise_tags);
    }
  }
}
