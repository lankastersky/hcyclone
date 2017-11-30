package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

public class ExercisesFragment extends Fragment
    implements OnExerciseSelectListener {

  static final String TAG = ExercisesFragment.class.getSimpleName();

  private RecyclerView recyclerView;
  private RecyclerView.Adapter adapter;
  private RecyclerView.LayoutManager layoutManager;
  private Exercise.LevelType level;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_exercises, container, false);
    getActivity().setTitle(getString(R.string.fragment_exericses_title));

    if (getArguments() != null) {
      level = (Exercise.LevelType) getArguments().get(BundleConstants.LEVEL_KEY);
    }

    recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);

    layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);

    App app = (App) getContext().getApplicationContext();
    ExerciseModel exerciseModel = app.getExerciseModel();
    Map<String, Exercise> exercisesMap = level == null
        ? exerciseModel.getExercises()
        : exerciseModel.getExercises(level);
//    List<Exercise> exercises = new ArrayList<>();
//    for (int i = 0; i < 10; i++) {
//      Exercise exercise =
//          new Exercise(
//              "Exercise " + i,
//              Exercise.ExerciseType.values()[i % Exercise.ExerciseType.values().length],
//              Exercise.LevelType.values()[i % Exercise.LevelType.values().length],
//              getString(R.string.step00_warmup00),
//              "",
//              "level1warmup0.gif"
//          );
//      exercises.add(exercise);
//    }

    adapter = new ExerciseRecyclerViewAdapter(exercisesMap, this);
    recyclerView.setAdapter(adapter);

    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    adapter = null;
  }

  @Override
  public void onListFragmentInteraction(Exercise exercise) {
    ExerciseFragment exerciseFragment = new ExerciseFragment();
    Bundle bundle = new Bundle();
    bundle.putString(BundleConstants.EXERCISE_ID_KEY, exercise.getId());
    exerciseFragment.setArguments(bundle);

    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (fragmentManager.findFragmentByTag(ExerciseFragment.TAG) == null) {
      fragmentTransaction
          .replace(
              ((ViewGroup)getView().getParent()).getId(),
              exerciseFragment,
              ExerciseFragment.TAG)
          .addToBackStack(ExerciseFragment.TAG)
          .commit();
    } else {
      Log.e(TAG, "Exercise fragment already exists");
    }
  }
}
