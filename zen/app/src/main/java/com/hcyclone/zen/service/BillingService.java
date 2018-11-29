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
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed.
 */
public final class BillingService implements PurchasesUpdatedListener {

  private static final String TAG = BillingService.class.getSimpleName();
  // Default value of mBillingClientResponseCode until BillingManager was not yeat initialized
  private static final int BILLING_MANAGER_NOT_INITIALIZED = -1;

  private BillingClient billingClient;
  /** True if billing service is connected now. */
  private boolean isServiceConnected;
  private int billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;
  private final List<Purchase> purchases = new ArrayList<>();

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
      queryPurchases();
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
        break;
      default:
        Log.w(TAG, "onPurchasesUpdated() got resultCode: " + resultCode);
    }
    billingUpdatesListener.onPurchasesUpdated(resultCode, purchases);
  }

  /** Clear the resources. */
  public void destroy() {
    Log.d(TAG, "Destroying the manager.");
    if (billingClient != null && billingClient.isReady()) {
      billingClient.endConnection();
      billingClient = null;
    }
  }

  /**
   * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
   * client connection response was not received yet.
   */
  public int getBillingClientResponseCode() {
    return billingClientResponseCode;
  }

  /** Requests sku details asynchronously. */
  public void querySkuDetailsAsync(@SkuType String itemType, List<String> skuList,
      SkuDetailsResponseListener listener) {
    Log.d(TAG, "Querying items of type " + itemType);
    // Creating a runnable from the request to use it inside our connection retry policy below.
    Runnable queryRequest = () -> {
      SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
      params.setSkusList(skuList).setType(itemType);
      billingClient.querySkuDetailsAsync(params.build(), listener);
    };

    executeServiceRequest(queryRequest);
  }


  /** Checks if subscriptions are supported for current client. */
  public boolean areSubscriptionsSupported() {
    int responseCode = billingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS);
    if (responseCode != BillingResponse.OK) {
      Log.w(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
    }
    return responseCode == BillingResponse.OK;
  }

  /**
   * Queries purchases across various use cases and delivers the result in a formalized way through
   * a listener.
   */
  public void queryPurchases() {
    Log.d(TAG, "Querying purchases");
    Runnable queryToExecute = () -> {
      PurchasesResult purchasesResult = billingClient.queryPurchases(SkuType.INAPP);
      // If there are subscriptions supported, we add subscription rows as well
      if (areSubscriptionsSupported()) {
        PurchasesResult subscriptionResult = billingClient.queryPurchases(SkuType.SUBS);
        Log.d(TAG, "Querying subscriptions result code: "
            + subscriptionResult.getResponseCode()
            + " res: " + subscriptionResult.getPurchasesList().size());

        if (subscriptionResult.getResponseCode() == BillingResponse.OK) {
          purchasesResult.getPurchasesList().addAll(subscriptionResult.getPurchasesList());
        } else {
          Log.e(TAG, "Got an error response trying to query subscription purchases");
        }
      } else if (purchasesResult.getResponseCode() == BillingResponse.OK) {
        Log.i(TAG, "Skipped subscription purchases query since they are not supported");
      } else {
        Log.w(TAG, "queryPurchases() got an error response code: "
            + purchasesResult.getResponseCode());
      }

      purchases.clear();
      purchases.addAll(purchasesResult.getPurchasesList());

      billingUpdatesListener.onPurchasesUpdated(
          purchasesResult.getResponseCode(), purchasesResult.getPurchasesList());
    };

    executeServiceRequest(queryToExecute);
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

  /**
   * Clears in-app purchases. Subscriptions can be cancelled on Play Store.
   * See https://developer.android.com/google/play/billing/billing_testing#cancelling
   * For debug only.
   */
  public void clearPurchases(Context context) {
    for (Purchase purchase: purchases) {
      Log.d(TAG, "Consuming " + purchase.getSku());
      consumeAsync(purchase.getPurchaseToken());
    }

    // Clear test purchases.
    Log.d(TAG, "Consuming " + context.getString(R.string.purchase_test_purchased));
    String purchaseToken = "inapp:" + context.getPackageName() + ":"
          + context.getString(R.string.purchase_test_purchased);
    consumeAsync(purchaseToken);
  }

  /** Consumes the purchase asynchronously. */
  private void consumeAsync(String purchaseToken) {
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
        billingClientResponseCode = billingResponseCode;
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
    void onPurchasesUpdated(@BillingResponse int result, List<Purchase> purchases);
  }
}
