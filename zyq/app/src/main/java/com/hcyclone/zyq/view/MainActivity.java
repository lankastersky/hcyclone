package com.hcyclone.zyq.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.hcyclone.zyq.Analytics;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise;

public class MainActivity extends AdsActivity
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

    showAds();
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
    switch (menuItemId) {
      case R.id.nav_level_1:
        currentLevel = Exercise.LevelType.LEVEL1;
        setPracticeFragment(currentLevel);
        expandAppBar(true);
        break;
      case R.id.nav_level_2:
        currentLevel = Exercise.LevelType.LEVEL2;
        setPracticeFragment(currentLevel);
        expandAppBar(true);
        break;
      case R.id.nav_level_3:
        currentLevel = Exercise.LevelType.LEVEL3;
        setPracticeFragment(currentLevel);
        expandAppBar(true);
        break;
      case R.id.nav_audio:
        replaceFragment(AudioFragment.class, AudioFragment.TAG);
        expandAppBar(false);
        break;
//      case R.id.nav_settings:
//        replaceFragment(SettingsFragment, SettingsFragment.class);
//        break;
      case R.id.nav_help:
        replaceFragment(HelpFragment.class, HelpFragment.TAG);
        expandAppBar(false);
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
    Analytics.getInstance().sendExerciseLevel(levelType);

    Fragment newFragment = getSupportFragmentManager().findFragmentByTag(
        PracticeFragment.class.getSimpleName());
    if (newFragment != null) {
      ((PracticeFragment) newFragment).updateLevelType(levelType);
    } else {
      replaceFragment(PracticeFragment.class, PracticeFragment.TAG);
    }
  }

  private void replaceFragment(Class<? extends Fragment> clazz, String tag) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Fragment fragment = fragmentManager.findFragmentByTag(tag);
    if (fragment == null) {
      try {
        fragment = clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        Crashlytics.logException(e);
        Log.e(TAG, "Failed to create fragment", e);
      }
    }
    fragmentTransaction.replace(R.id.content_container, fragment, tag).commit();
  }

  private void expandAppBar(boolean expand) {
    int actionBarHeight = (int) getResources().getDimension(R.dimen.action_bar_height);
    if (!expand) {
      // Calculate ActionBar height
      TypedValue tv = new TypedValue();
      if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        actionBarHeight =
            TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics())
                + getStatusBarHeight();
      }
    }

    ImageView appBarImageView = findViewById(R.id.app_bar_image_view);
    appBarImageView.setVisibility(expand ? View.VISIBLE : View.GONE);

    AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
    CoordinatorLayout.LayoutParams lp =
        (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
    lp.height = actionBarHeight;
    appBarLayout.setLayoutParams(lp);
    appBarLayout.setExpanded(expand,true);
    appBarLayout.setActivated(expand);

    CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
    collapsingToolbar.setTitleEnabled(expand);

    NestedScrollView scrollView = getWindow().getDecorView().findViewById(R.id.nested_scroll_view);
    scrollView.scrollTo(0, 0);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getWindow();
      //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(getResources().getColor(expand
          ? android.R.color.transparent
          : R.color.colorPrimaryDark));
    }
  }

  protected int getStatusBarHeight() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return 0;
    }
    int result = 0;
    int resourceId =
        getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }
}
