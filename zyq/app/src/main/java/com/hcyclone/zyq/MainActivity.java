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

  private Fragment currentFragment;
  private int selectedMenuItemId;

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

    if (savedInstanceState == null) {
      selectMenuItemPractice();
    }
  }

  private void selectMenuItemPractice() {
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();
    MenuItem menuItem = menu.findItem(R.id.nav_level_1);
    menuItem.setChecked(true);
    onNavigationItemSelected(menuItem);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
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

  private void selectMenuItem(int menuItemId) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment newFragment;
    switch (menuItemId) {
      case R.id.nav_level_1:
      case R.id.nav_level_2:
      case R.id.nav_level_3:
        newFragment = fragmentManager.findFragmentByTag(
            PracticeFragment.class.getSimpleName());
        replaceFragment(newFragment, PracticeFragment.class);
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
        Utils.getInstance().sendFeedback(this);
        break;
    }
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
}
