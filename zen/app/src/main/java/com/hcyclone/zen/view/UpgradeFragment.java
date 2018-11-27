package com.hcyclone.zen.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.hcyclone.zen.BillingProvider;
import com.hcyclone.zen.R;

public final class UpgradeFragment extends AppCompatDialogFragment {

  public static final String TAG = UpgradeFragment.class.getSimpleName();

  private BillingProvider billingProvider;
  private View mLoadingView;

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
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_upgrade, container, false);
    //mLoadingView = root.findViewById(R.id.screen_wait);
    Button upgradeButton = root.findViewById(R.id.upgrade_dialog_button);
    upgradeButton.setOnClickListener(v -> {
      // FeaturesService featuresService =
      //     ((App) getContext().getApplicationContext()).getFeaturesService();
      // featuresService.setExtendedVersion(true);
    });
    if (billingProvider != null) {
      handleManagerAndUiReady();
    }
    return root;
  }

  private void handleManagerAndUiReady() {
    billingProvider.getBillingManager().queryPurchases();
  }
}
