package com.hcyclone.zen;

import android.app.Activity;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed.
 */
public final class BillingManager implements PurchasesUpdatedListener {

  private static final String TAG = BillingManager.class.getSimpleName();
  // Default value of mBillingClientResponseCode until BillingManager was not yeat initialized
  private static final int BILLING_MANAGER_NOT_INITIALIZED  = -1;

  // create new Person
  private BillingClient billingClient;
  /** True if billing service is connected now. */
  private boolean isServiceConnected;
  private final BillingUpdatesListener billingUpdatesListener;
  private int billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

  public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {
    Log.d(TAG, "Creating Billing client.");
    billingClient = BillingClient.newBuilder(activity).setListener(this).build();
    billingUpdatesListener = updatesListener;

    // Start setup. This is asynchronous and the specified listener will be called
    // once setup completes.
    // It also starts to report all the new purchases through onPurchasesUpdated() callback.
    startServiceConnection(new Runnable() {
      @Override
      public void run() {
        // Notifying the listener that billing client is ready
        billingUpdatesListener.onBillingClientSetupFinished();
        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        Log.d(TAG, "Setup successful. Querying inventory.");
        queryPurchases();
      }
    });
  }

  /**
   * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
   * client connection response was not received yet.
   */
  public int getBillingClientResponseCode() {
    return billingClientResponseCode;
  }

  // PurchasesUpdatedListener

  @Override
  public void onPurchasesUpdated(int resultCode, List<Purchase> purchases) {
    if (resultCode == BillingResponse.OK) {
      for (Purchase purchase : purchases) {
        //handlePurchase(purchase);
      }
      //mBillingUpdatesListener.onPurchasesUpdated(mPurchases);
    } else if (resultCode == BillingResponse.USER_CANCELED) {
      Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
    } else {
      Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + resultCode);
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

  public void startServiceConnection(final Runnable executeOnSuccess) {
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

  /**
   * Query purchases across various use cases and deliver the result in a formalized way through
   * a listener.
   */
  public void queryPurchases() {
    List<String> skuList = new ArrayList<>();
    skuList.add("no_ads_subscription");
    if (Utils.isDebug()) {
      // See https://developer.android.com/google/play/billing/billing_testing#draft_apps
      skuList.add("android.test.purchased");
      skuList.add("android.test.canceled");
      skuList.add("android.test.item_unavailable");
    }
    querySkuDetailsAsync(SkuType.INAPP, skuList, new SkuDetailsResponseListener() {
      @Override
      public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        if (responseCode == BillingResponse.OK
            && skuDetailsList != null
            && !skuDetailsList.isEmpty()) {
          for (SkuDetails skuDetails : skuDetailsList) {
            Log.d(TAG, "Sku: " + skuDetails.toString());
          }
        } else {
          Log.e(TAG, "Failed to get sku details: " + responseCode);
        }
      }
    });
  }

  public void querySkuDetailsAsync(@SkuType final String itemType, final List<String> skuList,
      final SkuDetailsResponseListener listener) {
    // Creating a runnable from the request to use it inside our connection retry policy below
    Runnable queryRequest = new Runnable() {
      @Override
      public void run() {
        // Query the purchase async
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(itemType);
        billingClient.querySkuDetailsAsync(params.build(),
            new SkuDetailsResponseListener() {
              @Override
              public void onSkuDetailsResponse(int responseCode,
                  List<SkuDetails> skuDetailsList) {
                listener.onSkuDetailsResponse(responseCode, skuDetailsList);
              }
            });
      }
    };

    executeServiceRequest(queryRequest);
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
  }

}
