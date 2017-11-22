package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends Fragment
    implements OnExerciseSelectListener {

  private static final String TAG = ExercisesFragment.class.getSimpleName();

  private RecyclerView recyclerView;
  private RecyclerView.Adapter adapter;
  private RecyclerView.LayoutManager layoutManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_exercises, container, false);
    getActivity().setTitle(getString(R.string.fragment_exericses_title));

    recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);

    layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);

    List<Exercise> exercises = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Exercise exercise = new Exercise("Exercise " + i, i);
      exercises.add(exercise);
    }

    adapter = new ExerciseRecyclerViewAdapter(exercises, this);
    recyclerView.setAdapter(adapter);

    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    adapter = null;
  }

  @Override
  public void onListFragmentInteraction(Exercise item) {
    Toast.makeText(getActivity(), item.name, Toast.LENGTH_SHORT).show();
  }
}
