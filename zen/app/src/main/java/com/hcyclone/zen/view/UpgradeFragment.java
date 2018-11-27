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
import com.hcyclone.zen.service.BillingProvider;
import com.hcyclone.zen.R;

public final class UpgradeFragment extends AppCompatDialogFragment {

  public static final String TAG = UpgradeFragment.class.getSimpleName();

  private BillingProvider billingProvider;
  private View loadingView;
  private Button upgradeButton;
  private Button cancelButton;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof BillingProvider) {
      billingProvider = (BillingProvider) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement BillingProvider");
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

    setCancelable(false);

    loadingView = root.findViewById(R.id.screen_wait);
    upgradeButton = root.findViewById(R.id.upgrade_dialog_upgrade);
    upgradeButton.setOnClickListener(v -> {
      enableButtons(false);
      loadingView.setVisibility(View.VISIBLE);
      // FeaturesService featuresService =
      //     ((App) getContext().getApplicationContext()).getFeaturesService();
      // featuresService.setExtendedVersion(true);
    });

    cancelButton = root.findViewById(R.id.upgrade_dialog_cancel);
    cancelButton.setOnClickListener(v -> dismiss() );

    if (billingProvider != null) {
      handleManagerAndUiReady();
    }
    return root;
  }

  private void handleManagerAndUiReady() {
    billingProvider.getBillingService().queryPurchases();
  }

  private void enableButtons(boolean enable) {
    upgradeButton.setEnabled(enable);
    cancelButton.setEnabled(enable);

    int colorResId = enable ? R.color.colorPrimaryDark : R.color.colorPrimaryDisabled;
    upgradeButton.setBackgroundColor(ContextCompat.getColor(getActivity(), colorResId));
    cancelButton.setBackgroundColor(ContextCompat.getColor(getActivity(), colorResId));
  }
}
