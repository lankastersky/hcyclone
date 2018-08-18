package com.hcyclone.zyq.view;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.service.FeaturesService;

/** Loads and shows ads at the bottom. */
public abstract class AdsActivity extends AppCompatActivity {

  private static final String TAG = AdsActivity.class.getSimpleName();

  protected void showAds() {
    AdView adView = findViewById(R.id.adView);
    adView.setVisibility(View.GONE);
    if (FeaturesService.getInstance().getFeaturesType() == FeaturesService.FeaturesType.PAID) {
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
