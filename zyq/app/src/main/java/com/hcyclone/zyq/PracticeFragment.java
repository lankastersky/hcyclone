package com.hcyclone.zyq;

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
import android.widget.Toast;

public class PracticeFragment extends Fragment {
  private static final String TAG = PracticeFragment.class.getSimpleName();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_practice, container, false);
    getActivity().setTitle(getString(R.string.fragment_practice_title));

    setHasOptionsMenu(true);

    Button warmup = (Button) view.findViewById(R.id.warmup_button);
    warmup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getActivity(), "warmup", Toast.LENGTH_LONG).show();
      }
    });

    Button practice = (Button) view.findViewById(R.id.practice_button);
    practice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getActivity(), "practice", Toast.LENGTH_LONG).show();
      }
    });

    return view;
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

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_description) {
//      Toast.makeText(getActivity(), "description", Toast.LENGTH_SHORT).show();
      FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      try {
        fragmentTransaction
            .add(R.id.content_container, PracticeDescriptionFragment.class.newInstance())
            .addToBackStack(PracticeDescriptionFragment.TAG)
            .commit();
      } catch (java.lang.InstantiationException|IllegalAccessException e) {
        e.printStackTrace();
      }
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
