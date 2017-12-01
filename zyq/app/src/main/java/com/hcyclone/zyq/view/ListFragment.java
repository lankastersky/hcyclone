package com.hcyclone.zyq.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zyq.App;
import com.hcyclone.zyq.model.ExerciseModel;

import java.util.Collection;

/**
 * Base fragment containing lists.
 */
public abstract class ListFragment extends Fragment {

  protected RecyclerView recyclerView;
  protected RecyclerView.LayoutManager layoutManager;
  protected ExerciseModel exerciseModel;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    App app = (App) getContext().getApplicationContext();
    exerciseModel = app.getExerciseModel();
    return null;
  }

  protected void createListLayout(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);

    layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);

    recyclerView.setAdapter(adapter);
  }

  protected abstract Collection buildListItems();
}
