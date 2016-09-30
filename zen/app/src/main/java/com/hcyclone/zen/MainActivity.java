package com.hcyclone.zen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ChallengeListFragment.OnListFragmentInteractionListener,
    FirebaseAdapter.FirebaseAuthListener {

  private Fragment currentFragment;
  private ProgressDialog progress;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    if (savedInstanceState == null) {
      ChallengeFragment challengeListFragment = new ChallengeFragment();
      getSupportFragmentManager().beginTransaction().add(R.id.content_container,
          challengeListFragment, ChallengeFragment.class.getSimpleName()).commit();

      currentFragment = challengeListFragment;

      FirebaseAdapter firebaseAdapter = new FirebaseAdapter();
      progress = ProgressDialog.show(this,
          "Initialization", "Loading data from server...", true);
      firebaseAdapter.signIn(this);

    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    FragmentManager fragmentManager = getSupportFragmentManager();

    if (id == R.id.nav_challenge) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          ChallengeFragment.class.getSimpleName());
      replaceFragment(newFragment, ChallengeFragment.class);
    } else if (id == R.id.nav_journal) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          ChallengeListFragment.class.getSimpleName());
      replaceFragment(newFragment, ChallengeListFragment.class);
    } else if (id == R.id.nav_settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    } else if (id == R.id.nav_help) {

    } else if (id == R.id.nav_feedback) {

    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);

    return true;
  }

  @Override
  public void onAuthSuccess() {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");

    myRef.setValue("Hello, World!");
    progress.dismiss();
  }

  @Override
  public void onAuthError(Exception exception) {
    progress.dismiss();
  }

  private void replaceFragment(Fragment newFragment, Class clazz) {
    if (newFragment == currentFragment) {
      return;
    }
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.remove(currentFragment);
    if (newFragment == null) {
      try {
        newFragment = (Fragment) clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        System.out.println(e.toString());
      }
      fragmentTransaction.add(R.id.content_container,
          newFragment, clazz.getSimpleName()).commit();
    } else {
      fragmentTransaction.replace(R.id.content_container,
          newFragment, clazz.getSimpleName()).commit();
    }
    currentFragment = newFragment;
  }

  @Override
  public void onListFragmentInteraction(Challenge item) {
    Log.d(MainActivity.class.getSimpleName(), "onListFragmentInteraction: " + item.id);
    Intent intent = new Intent(this, ChallengeActivity.class);
    Bundle extras = new Bundle();
    extras.putString(ChallengeFragment.CHALLENGE_ID, item.id);
    intent.putExtras(extras);
    startActivity(intent);
  }
}
