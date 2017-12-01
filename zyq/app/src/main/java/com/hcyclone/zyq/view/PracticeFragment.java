package com.hcyclone.zyq.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shows practice start page.
 */
public class PracticeFragment extends ListFragment implements OnItemSelectListener<ExerciseGroup> {

  private static final String TAG = PracticeFragment.class.getSimpleName();

  private Exercise.LevelType level;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    MainActivity mainActivity = (MainActivity) context;
    level = mainActivity.getCurrentLevel();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_practice, container, false);

    setHasOptionsMenu(true);

    recyclerView = view.findViewById(R.id.practice_recycler_view);
    RecyclerView.Adapter adapter = new PracticeRecyclerViewAdapter(buildListItems(), this);
    createListLayout(recyclerView, adapter);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshUi();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.exercise_menu, menu);
    super.onCreateOptionsMenu(menu,inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_description) {
      Utils.showDescription(getContext());
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Collection<ExerciseGroup> buildListItems() {
    List<ExerciseGroup> items = new ArrayList<>();
    items.add(
        new ExerciseGroup(
            Exercise.ExerciseType.WARMUP.toString(),
            Exercise.ExerciseType.WARMUP));
    items.add(
        new ExerciseGroup(
            Exercise.ExerciseType.MEDITATION.toString(),
            Exercise.ExerciseType.MEDITATION));
    items.add(
        new ExerciseGroup(
            Exercise.ExerciseType.ADDITIONAL_MEDITATION.toString(),
            Exercise.ExerciseType.ADDITIONAL_MEDITATION));
    items.add(
        new ExerciseGroup(
            Exercise.ExerciseType.FINAL.toString(),
            Exercise.ExerciseType.FINAL));
    items.add(
        new ExerciseGroup(
            Exercise.ExerciseType.TREATMENT.toString(),
            Exercise.ExerciseType.TREATMENT));
    return items;
  }

  @Override
  public void onItemSelected(ExerciseGroup exerciseGroup) {
        Intent intent = new Intent(getContext(), ExercisesActivity.class);
        intent.putExtra(BundleConstants.EXERCISE_LEVEL_KEY, level);
        intent.putExtra(BundleConstants.EXERCISE_TYPE_KEY, exerciseGroup.type);
        startActivity(intent);
  }

  void updateLevelType(Exercise.LevelType levelType) {
    if (this.level == levelType) {
      return;
    }
    this.level = levelType;
    refreshUi();
  }

  private void refreshUi() {
    getActivity()
        .setTitle(String.format(getString(R.string.fragment_practice_title), level.getValue()));
  }
}
