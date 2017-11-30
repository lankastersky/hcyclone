package com.hcyclone.zyq.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * Base recycler view adapter.
 */
public abstract class ListAdapter<T, VH extends ListAdapter.ViewHolder<T>>
    extends RecyclerView.Adapter<VH> {

  private final Collection<T> items;
  private final OnItemSelectListener<T> listener;

  ListAdapter(Collection<T> items, OnItemSelectListener<T> listener) {
    this.items = items;
    this.listener = listener;
  }

  @Override
  public void onBindViewHolder(final VH holder, int position) {
    holder.item = Iterables.get(items, position);
    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          listener.onItemSelected(holder.item);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {

    final View view;
    T item;

    ViewHolder(View view) {
      super(view);
      this.view = view;
    }
  }
}
