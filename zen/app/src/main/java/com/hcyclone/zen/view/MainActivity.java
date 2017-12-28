package com.hcyclone.zen.view;

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

import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.AlarmService;
import com.hcyclone.zen.service.FirebaseService;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ChallengeListFragment.OnListFragmentInteractionListener {

  public static final String INTENT_PARAM_START_FROM_NOTIFICATION = "start_from_notification";
  private static final String TAG = MainActivity.class.getSimpleName();

  private ProgressBar progressBar;
  private boolean loadingChallenges;

  private static String getFragmentTag(Fragment fragment) {
    return fragment.getClass().getSimpleName();
  }

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

    progressBar = findViewById(R.id.progressBar);

    if (savedInstanceState == null) {
      loadingChallenges = true;
      selectChallengeMenuItem();

//      if (!Utils.isDebug()) {
        progressBar.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, FirebaseService.class);
        intent.putExtra(FirebaseService.INTENT_KEY_RECEIVER,
            new ChallengesResultReceiver(new Handler(), this));
        startService(intent);
//      } else {
//        ChallengeModel.getInstance().loadChallenges();
//        replaceFragment(ChallengeFragment.class);
//      }
    } else {
      loadingChallenges = false;
      progressBar.setVisibility(View.GONE);
      ChallengeModel.getInstance().loadChallenges();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (loadingChallenges) {
      return;
    }

    if (isStartFromNotification()) {
      Log.d(TAG, "Start from notification");
      getIntent().removeExtra(INTENT_PARAM_START_FROM_NOTIFICATION);
      selectChallengeMenuItem();
      replaceFragment(ChallengeFragment.class);
    }

    // Come here if challenges were loaded when activity was invisible.
    if (getSupportFragmentManager().getFragments().isEmpty()) {
      Log.d(TAG, "Start when challenged were loaded in background");
      selectChallengeMenuItem();
      replaceFragment(ChallengeFragment.class);
    }
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
    selectMenuItem(item.getItemId());
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onListFragmentInteraction(Challenge item) {
    Log.d(MainActivity.class.getSimpleName(), "onListFragmentInteraction: " + item.getId());
    Intent intent = new Intent(this, ChallengeActivity.class);
    Bundle extras = new Bundle();
    extras.putString(JournalChallengeFragment.CHALLENGE_ID, item.getId());
    intent.putExtras(extras);
    startActivity(intent);
  }

  private boolean isStartFromNotification() {
    Intent intent = getIntent();
    return intent.hasExtra(INTENT_PARAM_START_FROM_NOTIFICATION)
        && intent.getBooleanExtra(INTENT_PARAM_START_FROM_NOTIFICATION, false);
  }

  private void selectChallengeMenuItem() {
    NavigationView navigationView = findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();
    MenuItem menuItem = menu.findItem(R.id.nav_challenge);
    menuItem.setChecked(true);
  }

  private void selectMenuItem(int menuItemId) {
    switch (menuItemId) {
      case R.id.nav_challenge:
        replaceFragment(ChallengeFragment.class);
        break;
      case R.id.nav_journal:
        replaceFragment(ChallengeListFragment.class);
        break;
      case R.id.nav_statistics:
        replaceFragment(StatisticsFragment.class);
        break;
      case R.id.nav_settings:
        replaceFragment(SettingsFragment.class);
        break;
      case R.id.nav_help:
        replaceFragment(HelpFragment.class);
        break;
      case R.id.nav_feedback:
        Utils.sendFeedback(this);
        break;
      default:
        Log.d(TAG, "Wrong menu item " + menuItemId);
    }
  }

  private void replaceFragment(Class<? extends Fragment> clazz) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Fragment newFragment;
    try {
      newFragment = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      Log.d(TAG, e.toString());
      return;
    }
    fragmentTransaction.replace(R.id.content_container, newFragment,
        getFragmentTag(newFragment)).commit();
  }

  private void onChallengesLoaded(int resultCode) {
    loadingChallenges = false;
    progressBar.setVisibility(View.GONE);

    switch (resultCode) {
      case FirebaseService.RESULT_CODE_OK:
        AlarmService.getInstance().setAlarms();
        ChallengeModel.getInstance().loadChallenges();
        if (!AppLifecycleManager.isAppVisible()) {
          Log.d(TAG, "App in background. Skipping loading challenges");
          return;
        }
        replaceFragment(ChallengeFragment.class);
        break;
      case FirebaseService.RESULT_CODE_ERROR:
        Utils.buildDialog(getString(R.string.dialog_title_something_wrong),
            getString(R.string.dialog_text_failed_to_connect), this, null).show();
        break;
    }
  }

  private static class ChallengesResultReceiver extends ResultReceiver {

    private final WeakReference<MainActivity> mainActivityRef;

    private ChallengesResultReceiver(Handler handler, MainActivity mainActivity) {
      super(handler);
      mainActivityRef = new WeakReference<>(mainActivity);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);
      if (mainActivityRef.get() != null) {
        mainActivityRef.get().onChallengesLoaded(resultCode);
      }
    }
  }
}
