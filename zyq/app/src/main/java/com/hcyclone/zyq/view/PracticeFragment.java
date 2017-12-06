package com.hcyclone.zyq.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.hcyclone.zyq.BundleConstants;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;
import com.hcyclone.zyq.model.ExerciseGroup;
import com.hcyclone.zyq.model.ExerciseModel;

import java.util.Collection;

/**
 * Shows practice start page.
 */
public class PracticeFragment extends ListFragment implements OnItemSelectListener<ExerciseGroup> {

  public static final String TAG = PracticeFragment.class.getSimpleName();

  private Exercise.LevelType level;
  private String description;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    MainActivity mainActivity = (MainActivity) context;
    level = mainActivity.getCurrentLevel();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_practice, container, false);

    AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
    CoordinatorLayout.LayoutParams lp =
        (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
    lp.height = Utils.dpToPx(220);
    appBarLayout.setLayoutParams(lp);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getActivity().getWindow();
      //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
    }

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
    if (TextUtils.isEmpty(description)) {
      MenuItem item = menu.findItem(R.id.action_description);
      item.setVisible(false);
    }
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
  protected Collection<ExerciseGroup> buildListItems() {
    return ExerciseModel.buildExerciseGroups(level, getContext());
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
    CollapsingToolbarLayout collapsingToolbar = getActivity().findViewById(R.id.collapsing_toolbar);
    collapsingToolbar.setTitle(
        String.format(getString(R.string.fragment_practice_title), level.getValue()));
    description = exerciseModel.getPracticeDescription(level, getContext());
    getActivity().invalidateOptionsMenu();
  }
}
