package com.hcyclone.zyq;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Exercise}.
 */
public class ExerciseRecyclerViewAdapter
    extends RecyclerView.Adapter<ExerciseRecyclerViewAdapter.ViewHolder> {

  private final List<Exercise> values;
  private final OnExerciseSelectListener listener;

  ExerciseRecyclerViewAdapter(List<Exercise> items, OnExerciseSelectListener listener) {
    values = items;
    this.listener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_exercise_summary, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.item = values.get(position);
    holder.nameView.setText(holder.item.name);
    holder.typeView.setText(String.valueOf(holder.item.type));

    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          listener.onListFragmentInteraction(holder.item);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return values.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    final View view;
    final TextView nameView;
    final TextView typeView;

    Exercise item;

    ViewHolder(View view) {
      super(view);
      this.view = view;
      nameView = view.findViewById(R.id.exercise_name);
      typeView = view.findViewById(R.id.exercise_type);
    }
  }
}
