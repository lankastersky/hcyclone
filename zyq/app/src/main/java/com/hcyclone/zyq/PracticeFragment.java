package com.hcyclone.zyq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PracticeFragment extends Fragment {

  private static final String TAG = PracticeFragment.class.getSimpleName();
  private Exercise.LevelType level;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      MainActivity mainActivity = (MainActivity) context;
      level = mainActivity.getCurrentLevel();
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " is not MainActivity");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_practice, container, false);
    getActivity().setTitle(getString(R.string.fragment_practice_title));

    setHasOptionsMenu(true);

    Button warmup = view.findViewById(R.id.warmup_button);
    warmup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), WarmUpActivity.class);
        intent.putExtra(BundleConstants.LEVEL_KEY, level);
        startActivity(intent);

      }
    });

    Button practice = view.findViewById(R.id.practice_button);
    practice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), ExercisesActivity.class);
        intent.putExtra(BundleConstants.LEVEL_KEY, level);
        startActivity(intent);
      }
    });

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshUi();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_practice_menu, menu);
    super.onCreateOptionsMenu(menu,inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_description) {
      showDescription();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void updateLevelType(Exercise.LevelType levelType) {
    if (this.level == levelType) {
      return;
    }
    this.level = levelType;
    refreshUi();
  }

  private void refreshUi() {
    TextView practiceLevelTextView = getView().findViewById(R.id.practice_level);
    practiceLevelTextView.setText("Level " + level);
  }

  private void showDescription() {
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    try {
      if (fragmentManager.findFragmentByTag(PracticeDescriptionFragment.TAG) == null) {
        fragmentTransaction
            .replace(
                ((ViewGroup)getView().getParent()).getId(),
                PracticeDescriptionFragment.class.newInstance(),
                PracticeDescriptionFragment.TAG)
            .addToBackStack(PracticeDescriptionFragment.TAG)
            .commit();
      }
    } catch (java.lang.InstantiationException | IllegalAccessException e) {
      Log.e(TAG, "Failed to show practice description", e);
    }
  }
}
