package com.hcyclone.zyq.view;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcyclone.zyq.App;
import com.hcyclone.zyq.model.ExerciseModel;

import java.util.Collection;

/**
 * Base fragment containing lists.
 */
public abstract class ListFragment<T> extends Fragment {

  protected RecyclerView recyclerView;
  protected RecyclerView.LayoutManager layoutManager;
  protected ExerciseModel exerciseModel;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    App app = (App) getContext().getApplicationContext();
    exerciseModel = app.getExerciseModel();
  }

  protected void createListLayout(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);

    layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);

    recyclerView.setAdapter(adapter);
  }

  protected abstract Collection<T> buildListItems();
}
