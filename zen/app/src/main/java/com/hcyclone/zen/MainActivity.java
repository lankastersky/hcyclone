package com.hcyclone.zen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ChallengeListFragment.OnListFragmentInteractionListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  public static final String INTENT_PARAM_START_FROM_NOTIFICATION = "start_from_notification";
  private static final String SELECTED_MENU_ITEM_ID_PARAM = "selectedMenuItemId";

  private Fragment currentFragment;
  private int selectedMenuItemId;
  private ProgressBar progressBar;
  private ResultReceiver receiver = new ResultReceiver(new Handler()) {
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);

      progressBar.setVisibility(View.GONE);
      DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

      AlarmService.getInstance().setAlarms();

      ChallengeModel.getInstance().loadChallenges();

      switch (resultCode) {
        case FirebaseService.RESULT_CODE_OK:
          //if (AppLifecycleManager.isAppVisible()) {
            //showCurrentChallenge();
          //}
          ((ChallengeFragment) currentFragment).refresh();
          break;
        case FirebaseService.RESULT_CODE_ERROR:
          // TODO: show error.
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
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

    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    if (savedInstanceState == null) {
      selectChallengeMenuItem();

      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
      progressBar.setVisibility(View.VISIBLE);

      Intent intent = new Intent(this, FirebaseService.class);
      intent.putExtra(FirebaseService.INTENT_KEY_RECEIVER, receiver);
      startService(intent);
    } else {
      ChallengeModel.getInstance().loadChallenges();
    }
    showCurrentChallenge();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (isStartFromNotification()) {
      getIntent().removeExtra(INTENT_PARAM_START_FROM_NOTIFICATION);

      selectChallengeMenuItem();

      Fragment newFragment = getSupportFragmentManager().findFragmentByTag(
          ChallengeFragment.class.getSimpleName());
      replaceFragment(newFragment, ChallengeFragment.class);
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
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    selectedMenuItemId = item.getItemId();
    selectMenuItem(selectedMenuItemId);
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(SELECTED_MENU_ITEM_ID_PARAM, selectedMenuItemId);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    selectedMenuItemId = savedInstanceState.getInt(SELECTED_MENU_ITEM_ID_PARAM);
    selectMenuItem(selectedMenuItemId);
  }

  private boolean isStartFromNotification() {
    Intent intent = getIntent();
    if (intent.getExtras() != null
        && intent.getExtras().getBoolean(INTENT_PARAM_START_FROM_NOTIFICATION)) {
      return true;
    }
    return false;
  }

  private void selectChallengeMenuItem() {
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();
    MenuItem menuItem = menu.findItem(R.id.nav_challenge);
    menuItem.setChecked(true);
    selectedMenuItemId = menuItem.getItemId();
  }

  private void selectMenuItem(int menuItemId) {
    FragmentManager fragmentManager = getSupportFragmentManager();

    if (menuItemId == R.id.nav_challenge) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          ChallengeFragment.class.getSimpleName());
      replaceFragment(newFragment, ChallengeFragment.class);
    } else if (menuItemId == R.id.nav_journal) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          ChallengeListFragment.class.getSimpleName());
      replaceFragment(newFragment, ChallengeListFragment.class);
    } else if (menuItemId == R.id.nav_settings) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          SettingsFragment.class.getSimpleName());
      replaceFragment(newFragment, SettingsFragment.class);
    } else if (menuItemId == R.id.nav_help) {
      Fragment newFragment = fragmentManager.findFragmentByTag(
          HelpFragment.class.getSimpleName());
      replaceFragment(newFragment, HelpFragment.class);
    } else if (menuItemId == R.id.nav_feedback) {
      Utils.getInstance().sendFeedback(this);
    }
  }

  private void showCurrentChallenge() {
    currentFragment = new ChallengeFragment();
    getSupportFragmentManager().beginTransaction().add(R.id.content_container,
        currentFragment, ChallengeFragment.class.getSimpleName()).commit();
//        .commitAllowingStateLoss();
  }

  private void replaceFragment(Fragment newFragment, Class clazz) {
    if (newFragment == currentFragment && newFragment != null) {
      return;
    }
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (currentFragment != null) {
      fragmentTransaction.remove(currentFragment);
    }
    if (newFragment == null) {
      try {
        newFragment = (Fragment) clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        Log.d(TAG, e.toString());
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
    Log.d(MainActivity.class.getSimpleName(), "onListFragmentInteraction: " + item.getId());
    Intent intent = new Intent(this, ChallengeActivity.class);
    Bundle extras = new Bundle();
    extras.putString(ChallengeFragment.CHALLENGE_ID, item.getId());
    intent.putExtras(extras);
    startActivity(intent);
  }
}
