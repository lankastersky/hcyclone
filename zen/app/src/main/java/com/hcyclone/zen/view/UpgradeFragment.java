package com.hcyclone.zen.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.R;
import com.hcyclone.zen.service.BillingService;
import java.util.ArrayList;
import java.util.List;

/** Shows upgrade features. */
public final class UpgradeFragment extends AppCompatDialogFragment {

  public static final String TAG = UpgradeFragment.class.getSimpleName();
  public static final String ARG_PROMO_MODE = "arg_promo_mode";

  private static final String FEATURE_WITH_PRICE_FORMAT = "%s - %s";

  private BillingService billingService;
  private View loadingView;
  private Button buyButton;
  private Button subscribeButton;
  private Button cancelButton;
  private TextView title;
  private TextView priceBuyTextView;
  private TextView priceSubscribeTextView;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof MainActivity) {
      billingService = ((MainActivity) context).getBillingService();
    } else {
      throw new RuntimeException(context.toString() + " must implement MainActivity");
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_AppCompat_Dialog);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.fragment_upgrade, container, false);

    loadingView = root.findViewById(R.id.screen_wait);
    title = root.findViewById(R.id.upgrade_dialog_title);
    priceBuyTextView = root.findViewById(R.id.upgrade_dialog_price_buy);
    priceSubscribeTextView = root.findViewById(R.id.upgrade_dialog_price_subscribe);

    cancelButton = root.findViewById(R.id.upgrade_dialog_cancel);
    cancelButton.setOnClickListener(v -> dismiss());

    buyButton = root.findViewById(R.id.upgrade_dialog_buy);
    buyButton.setOnClickListener(v -> {
      Analytics.getInstance().sendBuyExtendedVersion();
      enableUi(false);
      String purchaseId;
      if (Utils.isDebug()) {
        purchaseId = getString(R.string.purchase_test_purchased);
      } else {
        purchaseId = getString(R.string.purchase_extended_version);
      }
      billingService.initiatePurchaseFlow(purchaseId, SkuType.INAPP, getActivity());
    });

    subscribeButton = root.findViewById(R.id.upgrade_dialog_subscribe);
    if (billingService.areSubscriptionsSupported()) {
      subscribeButton.setOnClickListener(v -> {
        Analytics.getInstance().sendSubscribeOnExtendedVersion();
        enableUi(false);
        String purchaseId = getString(R.string.purchase_extended_version_monthly_subscription);
        billingService.initiatePurchaseFlow(purchaseId, SkuType.SUBS, getActivity());
      });
    } else {
      subscribeButton.setVisibility(View.GONE);
      priceSubscribeTextView.setVisibility(View.GONE);
    }

    return root;
  }

  @Override
  public void onStart() {
    super.onStart();

    Bundle args = getArguments();
    updateMode(args.getBoolean(ARG_PROMO_MODE));

    enableUi(false);

    querySkuDetailsAsync((responseCode, skuDetailsList) -> {
      if (responseCode == BillingResponse.OK
          && skuDetailsList != null
          && !skuDetailsList.isEmpty()) {

        for (SkuDetails skuDetails : skuDetailsList) {
          Log.d(TAG, "Sku: " + skuDetails.toString());
        }
        refreshUi(skuDetailsList);
      } else {
        Log.e(TAG, "Failed to get sku details: " + responseCode);
      }
      enableUi(true);
    });
  }

  private void updateMode(boolean promoMode) {
    title.setText(promoMode
        ? getString(R.string.upgrade_dialog_promo_title)
        : getString(R.string.upgrade_dialog_title));
  }

  /** Enables controls. */
  private void enableUi(boolean enable) {
    enableButtons(enable);
    loadingView.setVisibility(enable ? View.GONE : View.VISIBLE);
  }

  private void refreshUi(List<SkuDetails> skuDetailsList) {
    for (SkuDetails skuDetails : skuDetailsList) {
      if (getContext().getString(R.string.purchase_extended_version).equals(skuDetails.getSku())) {
        String text = String.format(
            FEATURE_WITH_PRICE_FORMAT,
            getString(R.string.upgrade_dialog_one_time_purchase),
            skuDetails.getPrice());
        priceBuyTextView.setText(text);
        continue;
      }

      if (getContext().getString(R.string.purchase_extended_version_monthly_subscription)
          .equals(skuDetails.getSku())) {

        String text = String.format(
            FEATURE_WITH_PRICE_FORMAT,
            getString(R.string.upgrade_dialog_subscription),
            skuDetails.getPrice());
        priceSubscribeTextView.setText(text);
        continue;
      }

      if (Utils.isDebug()) {
        if (getContext().getString(R.string.purchase_test_purchased).equals(skuDetails.getSku())) {
          String text = String.format(
              FEATURE_WITH_PRICE_FORMAT,
              getString(R.string.upgrade_dialog_one_time_purchase),
              skuDetails.getPrice());
          priceBuyTextView.setText(text);
        }
      }
    }
  }

  /**
   * Query purchases across various use cases and deliver the result in a formalized way through
   * a listener.
   */
  private void querySkuDetailsAsync(SkuDetailsResponseListener listener) {
    // Request in-app purchases.
    List<String> skuInAppList = new ArrayList<>();
    skuInAppList.add(getContext().getString(R.string.purchase_extended_version));
    if (Utils.isDebug()) {
      // See https://developer.android.com/google/play/billing/billing_testing#draft_apps
      skuInAppList.add(getContext().getString(R.string.purchase_test_purchased));
      //skuList.add("android.test.canceled");
      //skuList.add("android.test.item_unavailable");
    }
    billingService.querySkuDetailsAsync(SkuType.INAPP, skuInAppList, listener);

    // Request subscriptions.
    if (billingService.areSubscriptionsSupported()) {
      List<String> skuSubsList = new ArrayList<>();
      skuSubsList.add(
          getContext().getString(R.string.purchase_extended_version_monthly_subscription));
      if (Utils.isDebug()) {
        skuSubsList.add(getContext().getString(R.string.purchase_test_purchased));
      }
      billingService.querySkuDetailsAsync(SkuType.SUBS, skuSubsList, listener);
    }
  }

  private void enableButtons(boolean enable) {
    buyButton.setEnabled(enable);
    subscribeButton.setEnabled(enable);
    cancelButton.setEnabled(enable);

    int colorResId = enable ? R.color.colorPrimaryDark : R.color.colorPrimaryDisabled;
    buyButton.setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));
    subscribeButton.setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));
    cancelButton.setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));
  }
}
