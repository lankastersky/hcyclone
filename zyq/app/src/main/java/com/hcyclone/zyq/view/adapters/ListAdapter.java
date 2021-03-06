package com.hcyclone.zyq.view.adapters;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import com.google.common.collect.Iterables;
import com.hcyclone.zyq.view.OnItemSelectListener;

import java.util.Collection;

/**
 * Base recycler view adapter.
 */
public abstract class ListAdapter<T, VH extends ListAdapter.ViewHolder<T>>
    extends RecyclerView.Adapter<VH> {

  protected Collection<T> items;
  protected final OnItemSelectListener<T> listener;

  ListAdapter(Collection<T> items, OnItemSelectListener<T> listener) {
    this.items = items;
    this.listener = listener;
  }

  @Override
  public void onBindViewHolder(final VH holder, int position) {
    holder.item = Iterables.get(items, position);
    holder.view.setOnClickListener(v -> {
      if (listener != null) {
        listener.onItemSelected(holder.item);
      }
    });
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public void setItems(Collection<T> items) {
    this.items = items;
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
