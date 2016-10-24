package com.hcyclone.zen;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.ResultReceiver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ChallengeListFragment.OnListFragmentInteractionListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private Fragment currentFragment;
  private ProgressDialog progress;
  private ResultReceiver receiver = new ResultReceiver(new Handler()) {
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);
      progress.dismiss();
      switch (resultCode) {
        case FirebaseService.RESULT_CODE_OK:
          showCurrentChallenge();
          break;
        case FirebaseService.RESULT_CODE_ERROR:
          // TODO: show error.
          break;
      }
    }
  };

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

    progress = ProgressDialog.show(this, "Initialization", "Loading data from server...", true);

    Intent intent = new Intent(this, FirebaseService.class);
    intent.putExtra(FirebaseService.INTENT_KEY_RECEIVER, receiver);
    startService(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Don't receive notifications if app is started.
    Log.d(TAG, "Disable alarm receiver");
    ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
    getPackageManager().setComponentEnabledSetting(receiver,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "Enable alarm receiver");
    ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
    getPackageManager().setComponentEnabledSetting(receiver,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
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
    if (id == R.id.action_share) {
      // TODO: implement
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
      Fragment newFragment = fragmentManager.findFragmentByTag(
          HelpFragment.class.getSimpleName());
      replaceFragment(newFragment, HelpFragment.class);
    } else if (id == R.id.nav_feedback) {
      Utils.getInstance().sendFeedback(this);
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);

    return true;
  }

  private void showCurrentChallenge() {
    currentFragment = new ChallengeFragment();
    // TODO: don't call commitAllowingStateLoss().
    getSupportFragmentManager().beginTransaction().add(R.id.content_container,
        currentFragment, ChallengeFragment.class.getSimpleName()).commitAllowingStateLoss();
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
