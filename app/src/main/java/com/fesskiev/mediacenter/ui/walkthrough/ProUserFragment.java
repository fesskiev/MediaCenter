package com.fesskiev.mediacenter.ui.walkthrough;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.billing.InAppBillingActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.billing.Billing;
import com.fesskiev.mediacenter.utils.billing.Inventory;
import com.fesskiev.mediacenter.utils.billing.Purchase;

import io.reactivex.Observable;;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ProUserFragment extends Fragment implements View.OnClickListener {

    public static ProUserFragment newInstance() {
        return new ProUserFragment();
    }

    private Disposable subscription;
    private Billing billing;

    private TextView adMobText;
    private Button[] buttons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pro_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adMobText = view.findViewById(R.id.adMobText);

        buttons = new Button[]{
                view.findViewById(R.id.buttonRemoveAdMob),
                view.findViewById(R.id.buttonSkipRemoveAdMob)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        createBilling();
    }

    private void createBilling() {
        billing = new Billing(getActivity());
        billing.connect(this::fetchBillingState);
        billing.setOnPurchaseProductListener(new Billing.OnPurchaseProductListener() {
            @Override
            public void onProductPurchased(Purchase purchase) {

            }

            @Override
            public void onProductPurchaseError() {
                showErrorPurchaseView();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppSettingsManager.getInstance().isUserPro()) {
            showSuccessPurchase();
        }
    }


    private void fetchBillingState(boolean connected) {
        if (connected) {
            subscription = billing.isBillingSupported()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(supported -> {
                        if (supported) {
                            return Observable.zip(billing.getProducts(), billing.getPurchases(), Inventory::new);
                        } else {
                            showBillingNotSupportedView();
                            return Observable.just(null);
                        }
                    }).subscribe(this::checkInventory, this::showThrowable);
        } else {
            showConnectedErrorView();
        }
    }

    private void checkInventory(Inventory inventory) {
        if (inventory != null) {
            Purchase purchase = inventory.isProductPurchased(Billing.PRODUCT_SKU);
            if (purchase != null) {
                showSuccessPurchase();
                AppSettingsManager.getInstance().setUserPro(true);
            }
        }
    }


    private void showSuccessPurchase() {
        adMobText.setText(getString(R.string.ad_mob_remove_success));
        adMobText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_walk_through_ok, 0, 0);

        hideButtons();
        notifyProUserGranted();

    }

    private void showSkipPurchase() {
        adMobText.setText(getString(R.string.ad_mob_remove_skip));
        adMobText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_walk_through_ok, 0, 0);
    }

    private void showErrorPurchaseView() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.billing_error_purchase), Snackbar.LENGTH_LONG).show();
    }

    private void showBillingNotSupportedView() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.billing_error_not_support), Snackbar.LENGTH_LONG).show();
    }

    private void showConnectedErrorView() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.billing_error_connection), Snackbar.LENGTH_LONG).show();
    }

    private void showThrowable(Throwable throwable) {
        Utils.showCustomSnackbar(getView(), getContext(),
                throwable.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
        billing.disconnect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRemoveAdMob:
                startBillingActivity();
                break;
            case R.id.buttonSkipRemoveAdMob:
                showSkipPurchase();
                notifyProUserGranted();
                hideButtons();
                break;
        }
    }


    private void startBillingActivity() {
        startActivity(new Intent(getActivity(), InAppBillingActivity.class));
    }


    private void notifyProUserGranted() {
        WalkthroughFragment walkthroughFragment = (WalkthroughFragment) getFragmentManager().
                findFragmentByTag(WalkthroughFragment.class.getName());
        if (walkthroughFragment != null) {
            walkthroughFragment.proUserGranted();
        }
    }

    private void hideButtons() {
        for (Button button : buttons) {
            button.setVisibility(View.GONE);
        }
    }
}
