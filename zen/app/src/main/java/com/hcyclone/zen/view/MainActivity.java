package com.hcyclone.zen.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hcyclone.zen.App;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.service.BillingService;
import com.hcyclone.zen.service.BillingService.BillingUpdatesListener;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.service.ChallengesLoader;
import com.hcyclone.zen.service.FeaturesService;
import com.hcyclone.zen.service.FeaturesService.FeaturesType;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ChallengeListFragment.OnListFragmentInteractionListener,
    ChallengesLoader.ChallengesLoadListener,
    BillingUpdatesListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  public static final String INTENT_PARAM_START_FROM_NOTIFICATION = "start_from_notification";

  private static final String KEY_LAST_FRAGMENT_TAG = "last_fragment_tag";

  private ProgressBar progressBar;
  private DrawerLayout drawer;
  private NavigationView navigationView;
  private boolean loadingChallenges;
  private String lastFragmentTag;
  private BillingService billingService;
  private ChallengeModel challengeModel;
  private FeaturesService featuresService;
  private UpgradeFragment upgradeFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    progressBar = findViewById(R.id.progressBar);

    loadingChallenges = true;
    progressBar.setVisibility(View.VISIBLE);
    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    App app = (App) getApplication();
    challengeModel = app.getChallengeModel();
    featuresService = app.getFeaturesService();

    if (savedInstanceState != null) {
      lastFragmentTag = savedInstanceState.getString(KEY_LAST_FRAGMENT_TAG);
    }

    billingService = new BillingService(this, this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (loadingChallenges) {
      // We load using background service which is not available on Android 8+ in background. So
      // we have to wait for the activity to be in foreground (started) first.
      // See https://developer.android.com/about/versions/oreo/background#services
      new Handler().post(() -> {
        new ChallengesLoader(this, this).loadChallenges(this);
      });
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    refreshUi();

    if (loadingChallenges) {
      return;
    }

    if (isStartFromNotification()) {
      Log.d(TAG, "Start from notification");
      getIntent().removeExtra(INTENT_PARAM_START_FROM_NOTIFICATION);
      selectMenuItem(ChallengeFragment.TAG);
      replaceFragment(ChallengeFragment.TAG);
    }
//    // Come here if challenges were loaded when activity was invisible.
//    if (getSupportFragmentManager().getFragments().isEmpty()) {
//      Log.d(TAG, "Start when challenges were loaded in background");
//      selectMenuItem();
//      replaceFragment(ChallengeFragment.TAG);
//    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(KEY_LAST_FRAGMENT_TAG, lastFragmentTag);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "Destroy");
    super.onDestroy();
    billingService.destroy();
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

  // OnListFragmentInteractionListener

  @Override
  public void onListFragmentInteraction(Challenge item) {
    Log.d(MainActivity.class.getSimpleName(), "onListFragmentInteraction: " + item.getId());
    Intent intent = new Intent(this, ChallengeActivity.class);
    Bundle extras = new Bundle();
    extras.putString(JournalChallengeFragment.CHALLENGE_ID, item.getId());
    intent.putExtras(extras);
    startActivity(intent);
  }

  // ChallengesLoadListener

  @Override
  public void onChallengesLoaded() {
    loadingChallenges = false;
    progressBar.setVisibility(View.GONE);
    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

    if (!AppLifecycleManager.isAppVisible()) {
      Log.d(TAG, "App in background. Skipping showing challenges");
      return;
    }
    if (challengeModel.getCurrentChallenge() != null) {
      boolean fromNotification = isStartFromNotification();
      if (fromNotification || lastFragmentTag == null) {
        if (fromNotification) {
          Log.d(TAG, "Load from notification");
          getIntent().removeExtra(INTENT_PARAM_START_FROM_NOTIFICATION);
        }
        selectMenuItem(ChallengeFragment.TAG);
        replaceFragment(ChallengeFragment.TAG);
      } else {
        selectMenuItem(lastFragmentTag);
        replaceFragment(lastFragmentTag);
      }
    } else {
      Utils.buildDialog(getString(R.string.dialog_title_something_wrong),
          getString(R.string.dialog_text_failed_to_connect), this, null).show();
    }
  }

  @Override
  public void onError(Exception e) {
    loadingChallenges = false;
    progressBar.setVisibility(View.GONE);
    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

    Utils.buildDialog(getString(R.string.dialog_title_something_wrong),
        getString(R.string.dialog_text_failed_to_connect), this, null).show();
  }

  // BillingUpdatesListener

  @Override
  public void onBillingClientSetupFinished() {
    Log.d(TAG, "On billing client setup finished");
  }

  @Override
  public void onConsumeFinished(String token, int result) {
    Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);
    if (result == BillingResponse.OK) {
      Log.d(TAG, "Consumption successful.");
    } else {
      Log.e(TAG, "Error while consuming: " + result);
    }
  }

  @Override
  public void onPurchasesUpdated(List<Purchase> purchases) {
    for (Purchase purchase : purchases) {
      Log.d(TAG, purchase.getSku() + " purchased");
    }
    // We have only one purchase for now.
    //if (getString(R.string.purchase_extended_version).equals(purchase.getSku())) {
    featuresService.storeExtendedVersion();
    showUpgradeDialog(false);
    refreshUi();
    //}
  }

  @Override
  public void onPurchasesCancelled() {
    if (upgradeFragment != null && upgradeFragment.isVisible()) {
      upgradeFragment.enableUi(true);
    }
    refreshUi();
  }

  public BillingService getBillingService() {
    return billingService;
  }

  /** Shows upgrade dialog. */
  public void showUpgradeDialog(boolean show) {
    if (show && featuresService.getFeaturesType() == FeaturesType.PAID) {
      Utils.buildDialog(
          "You are using extended version",
          "Features available:\n- Different levels of challenges\n- Statistics charts\n- No ads",
          this,
          null).show();
      return;
    }
    if (upgradeFragment == null) {
      upgradeFragment = new UpgradeFragment();
    }
    if (show) {
      upgradeFragment.show(getSupportFragmentManager(), UpgradeFragment.TAG);
    } else if (upgradeFragment.isVisible()) {
      upgradeFragment.dismiss();
    }
  }

  private void refreshUi() {
    boolean extended = featuresService.getFeaturesType() == FeaturesType.PAID;

    // Update charts.
    Menu navigationMenu = navigationView.getMenu();
    MenuItem item = navigationMenu.findItem(R.id.nav_statistics);
    item.setVisible(extended);

    showAds(!extended);
  }

  private boolean isStartFromNotification() {
    Intent intent = getIntent();
    return intent.hasExtra(INTENT_PARAM_START_FROM_NOTIFICATION)
        && intent.getBooleanExtra(INTENT_PARAM_START_FROM_NOTIFICATION, false);
  }

  private void selectMenuItem(String className) {
    NavigationView navigationView = findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();
    MenuItem menuItem;
     if (className.equals(ChallengeListFragment.TAG)) {
      menuItem = menu.findItem(R.id.nav_journal);
    } else if (className.equals(StatisticsFragment.TAG)) {
      menuItem = menu.findItem(R.id.nav_statistics);
    } else if (className.equals(SettingsFragment.TAG)) {
      menuItem = menu.findItem(R.id.nav_settings);
    } else if (className.equals(HelpFragment.TAG)) {
      menuItem = menu.findItem(R.id.nav_help);
    } else { // if (className.equals(ChallengeFragment.TAG))
      menuItem = menu.findItem(R.id.nav_challenge);
    }
    menuItem.setChecked(true);
  }

  private void selectMenuItem(int menuItemId) {
    switch (menuItemId) {
      case R.id.nav_challenge:
        replaceFragment(ChallengeFragment.TAG);
        break;
      case R.id.nav_journal:
        replaceFragment(ChallengeListFragment.TAG);
        break;
      case R.id.nav_statistics:
        replaceFragment(StatisticsFragment.TAG);
        break;
      case R.id.nav_settings:
        replaceFragment(SettingsFragment.TAG);
        break;
      case R.id.nav_help:
        replaceFragment(HelpFragment.TAG);
        break;
      case R.id.nav_upgrade:
        showUpgradeDialog(true);
        break;
      case R.id.nav_feedback:
        Utils.sendFeedback(this);
        break;
      default:
        Log.e(TAG, "Wrong menu item " + menuItemId);
    }
  }

  private void replaceFragment(String className) {
    Class<?  extends Fragment> clazz;
    try {
      clazz = Class.forName(className).asSubclass(Fragment.class);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, e.toString());
      return;
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    // Even if this fragment is already active, replace it to force the refresh.
    // if (fragmentManager.findFragmentByTag(getTag(clazz)) != null) {
    //   return;
    // }
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Fragment newFragment;
    try {
      newFragment = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      Log.d(TAG, e.toString());
      return;
    }
    fragmentTransaction.replace(R.id.content_container, newFragment, className)
        .commitAllowingStateLoss();

    lastFragmentTag = className;
  }

  private void showAds(boolean show) {
    AdView adView = findViewById(R.id.adView);
    adView.setVisibility(View.GONE);
    if (!show) {
      return;
    }
    AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
    if (Utils.isDebug()) {
      adRequestBuilder
          .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
          .addTestDevice("C1E5694CEA6750AFC77596E5C0295F9B"); // Nexus 5
    }
    AdRequest adRequest = adRequestBuilder.build();
    adView.loadAd(adRequest);
    adView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        adView.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAdFailedToLoad(int errorCode) {
        Log.w(TAG, "Failed to load ad: " + String.valueOf(errorCode));
      }

      @Override
      public void onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
      }

      @Override
      public void onAdLeftApplication() {
        // Code to be executed when the user has left the app.
      }

      @Override
      public void onAdClosed() {
        // Code to be executed when when the user is about to return
        // to the app after tapping on an ad.
      }
    });
  }
}
