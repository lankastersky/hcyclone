package com.hcyclone.zyq;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String SELECTED_MENU_ITEM_ID_PARAM = "selectedMenuItemId";

  private int selectedMenuItemId;
  private Exercise.LevelType currentLevel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    if (savedInstanceState == null) {
      selectMenuItemPractice();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

  Exercise.LevelType getCurrentLevel() {
    return currentLevel;
  }

  private void selectMenuItem(int menuItemId) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment newFragment;
    switch (menuItemId) {
      case R.id.nav_level_1:
        currentLevel = Exercise.LevelType.LEVEL1;
        setPracticeFragment(currentLevel);
        break;
      case R.id.nav_level_2:
        currentLevel = Exercise.LevelType.LEVEL2;
        setPracticeFragment(currentLevel);
        break;
      case R.id.nav_level_3:
        currentLevel = Exercise.LevelType.LEVEL3;
        setPracticeFragment(currentLevel);
        break;
      case R.id.nav_exercises:
        newFragment = fragmentManager.findFragmentByTag(
            ExercisesFragment.class.getSimpleName());
        replaceFragment(newFragment, ExercisesFragment.class);
        break;
      case R.id.nav_settings:
        newFragment = fragmentManager.findFragmentByTag(
            SettingsFragment.class.getSimpleName());
        replaceFragment(newFragment, SettingsFragment.class);
        break;
      case R.id.nav_help:
        newFragment = fragmentManager.findFragmentByTag(
            HelpFragment.class.getSimpleName());
        replaceFragment(newFragment, HelpFragment.class);
        break;
      case R.id.nav_feedback:
        Utils.sendFeedback(this);
        break;
    }
  }

  private void selectMenuItemPractice() {
    NavigationView navigationView = findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();
    MenuItem menuItem = menu.findItem(R.id.nav_level_1);
    menuItem.setChecked(true);
    onNavigationItemSelected(menuItem);
  }

  private void setPracticeFragment(Exercise.LevelType levelType) {
    Fragment newFragment = getSupportFragmentManager().findFragmentByTag(
        PracticeFragment.class.getSimpleName());
    if (newFragment != null) {
      ((PracticeFragment) newFragment).updateLevelType(levelType);
    } else {
      replaceFragment(newFragment, PracticeFragment.class);
    }
  }

  private void replaceFragment(Fragment newFragment, Class clazz) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    if (newFragment == null) {
      try {
        newFragment = (Fragment) clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        Log.d(TAG, e.toString());
      }
    }
      fragmentTransaction
          .replace(R.id.content_container, newFragment, clazz.getSimpleName())
          .commit();
  }
}
