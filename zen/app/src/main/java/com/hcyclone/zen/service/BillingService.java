package com.hcyclone.zen.service;

import android.app.Activity;
import android.content.Context;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.hcyclone.zen.Log;
import java.util.List;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed.
 */
public final class BillingService implements PurchasesUpdatedListener {

  private static final String TAG = BillingService.class.getSimpleName();

  private BillingClient billingClient;
  /** True if billing service is connected now. */
  private boolean isServiceConnected;

  private final BillingUpdatesListener billingUpdatesListener;

  public BillingService(BillingUpdatesListener updatesListener, Context context) {
    Log.d(TAG, "Creating Billing client.");
    billingClient = BillingClient.newBuilder(context).setListener(this).build();
    billingUpdatesListener = updatesListener;

    // Start setup. This is asynchronous and the specified listener will be called
    // once setup completes.
    // It also starts to report all the new purchases through onPurchasesUpdated() callback.
    startServiceConnection(() -> {
      // Notifying the listener that billing client is ready
      billingUpdatesListener.onBillingClientSetupFinished();
      // IAB is fully set up. Now, let's get an inventory of stuff we own.
      Log.d(TAG, "Setup successful. Querying inventory.");
      //queryPurchases();
    });
  }

  // PurchasesUpdatedListener

  @Override
  public void onPurchasesUpdated(int resultCode, List<Purchase> purchases) {
    switch (resultCode) {
      case BillingResponse.OK:
        //TODO: verify signature. See https://goo.gl/6d655g
        // for (Purchase purchase : purchases) {
        // }
        billingUpdatesListener.onPurchasesUpdated(purchases);
        break;
      default:
        Log.w(TAG, "onPurchasesUpdated() got resultCode: " + resultCode);
        billingUpdatesListener.onPurchasesCancelled();
    }
  }

  /** Clear the resources. */
  public void destroy() {
    Log.d(TAG, "Destroying the manager.");
    if (billingClient != null && billingClient.isReady()) {
      billingClient.endConnection();
      billingClient = null;
    }
  }

  /** Checks if subscriptions are supported for current client. */
  public boolean areSubscriptionsSupported() {
    int responseCode = billingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS);
    if (responseCode != BillingResponse.OK) {
      Log.w(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
    }
    return responseCode == BillingResponse.OK;
  }

  /** Requests sku details asynchronously. */
  public void querySkuDetailsAsync(@SkuType String itemType, List<String> skuList,
      SkuDetailsResponseListener listener) {
    // Creating a runnable from the request to use it inside our connection retry policy below.
    Runnable queryRequest = () -> {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(skuList).setType(itemType);
      billingClient.querySkuDetailsAsync(params.build(), listener);
    };

    executeServiceRequest(queryRequest);
  }

  /** Starts a purchase flow. */
  public void initiatePurchaseFlow(String skuId, @SkuType String billingType, Activity activity) {
    Runnable purchaseFlowRequest = () -> {
      Log.d(TAG, "Launching in-app purchase flow.");
      BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
          .setSku(skuId).setType(billingType).build();
      billingClient.launchBillingFlow(activity, purchaseParams);
    };

    executeServiceRequest(purchaseFlowRequest);
  }

  /** Consumes the purchase asynchronously. */
  public void consumeAsync(String purchaseToken) {
    executeServiceRequest(() ->
        billingClient.consumeAsync(purchaseToken, (responseCode, purchaseToken1) ->
            billingUpdatesListener.onConsumeFinished(purchaseToken1, responseCode)));
  }

  private void startServiceConnection(final Runnable executeOnSuccess) {
    billingClient.startConnection(new BillingClientStateListener() {

      @Override
      public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
        Log.d(TAG, "Setup finished. Response code: " + billingResponseCode);

        if (billingResponseCode == BillingResponse.OK) {
          isServiceConnected = true;
          if (executeOnSuccess != null) {
            executeOnSuccess.run();
          }
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        isServiceConnected = false;
      }
    });
  }

  private void executeServiceRequest(Runnable runnable) {
    if (isServiceConnected) {
      runnable.run();
    } else {
      // If billing service was disconnected, we try to reconnect 1 time.
      // (feel free to introduce your retry policy here).
      startServiceConnection(runnable);
    }
  }

  /**
   * Listener to the updates that happen when purchases list was updated or consumption of the
   * item was finished.
   */
  public interface BillingUpdatesListener {
    void onBillingClientSetupFinished();
    void onConsumeFinished(String token, @BillingResponse int result);
    void onPurchasesUpdated(List<Purchase> purchases);
    void onPurchasesCancelled();
  }

}
