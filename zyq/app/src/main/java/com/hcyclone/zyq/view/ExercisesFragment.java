package com.hcyclone.zyq.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zyq.Analytics;
import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseModel;
import com.hcyclone.zyq.view.adapters.ExerciseRecyclerViewAdapter;

import java.util.Collection;
import java.util.Map;

/**
 * Shows exercises as a list.
 */
public class ExercisesFragment extends ListFragment implements OnItemSelectListener<Exercise> {

  static final String TAG = ExercisesFragment.class.getSimpleName();

  private Exercise.LevelType level;
  private Exercise.ExerciseType type;
  private String description;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_exercises, container, false);

    setHasOptionsMenu(true);

    if (getArguments() != null) {
      level = (Exercise.LevelType) getArguments().get(BundleConstants.EXERCISE_LEVEL_KEY);
      type = (Exercise.ExerciseType) getArguments().get(BundleConstants.EXERCISE_TYPE_KEY);
      description = exerciseModel.getPracticeDescription(level, type, getContext());
    }

    getActivity().setTitle(ExerciseModel.exerciseTypeToString(type, getContext()));

    recyclerView = view.findViewById(R.id.exercises_recycler_view);
    RecyclerView.Adapter adapter = new ExerciseRecyclerViewAdapter(buildListItems(), this);
    createListLayout(recyclerView, adapter);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    Analytics.getInstance().sendExerciseType(type);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.exercise_menu, menu);
    if (TextUtils.isEmpty(description)) {
      MenuItem item = menu.findItem(R.id.action_description);
      item.setVisible(false);
    }
    // Hide video icon
    MenuItem item = menu.findItem(R.id.action_video);
    item.setVisible(false);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_description) {
      Utils.showDescription(description, getContext());
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Collection<Exercise> buildListItems() {
    Map<String, Exercise> exercisesMap = level == null
        ? exerciseModel.getExercises()
        : exerciseModel.getExercises(level, type);
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
    return exercisesMap.values();
  }

  @Override
  public void onItemSelected(Exercise exercise) {
    switch (exercise.type) {
      case WARMUP:
      case FINAL:
        showExerciseFlow(exercise);
        break;
      case TREATMENT:
      case ADDITIONAL_MEDITATION:
      case MEDITATION:
        showExercise(exercise);
        break;
      default:
        throw new AssertionError("No such exercise type: " + exercise.type);
    }
  }

  private void showExercise(Exercise exercise) {
    Intent intent = new Intent(getContext(), ExerciseActivity.class);
    intent.putExtra(BundleConstants.EXERCISE_ID_KEY, exercise.getId());
    startActivity(intent);
  }

  private void showExerciseFlow(Exercise exercise) {
    Intent intent = new Intent(getContext(), ExerciseFlowActivity.class);
    intent.putExtra(BundleConstants.EXERCISE_ID_KEY, exercise.getId());
    intent.putExtra(BundleConstants.EXERCISE_LEVEL_KEY, level);
    startActivity(intent);
  }
}
