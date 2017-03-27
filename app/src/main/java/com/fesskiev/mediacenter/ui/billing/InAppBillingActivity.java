package com.fesskiev.mediacenter.ui.billing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.billing.Billing;
import com.fesskiev.mediacenter.utils.billing.Inventory;
import com.fesskiev.mediacenter.utils.billing.Product;
import com.fesskiev.mediacenter.utils.billing.Purchase;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InAppBillingActivity extends AppCompatActivity {

    private Subscription subscription;
    private Billing billing;

    private Product product;

    private FloatingActionButton fab;
    private TextView textData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
        fab = (FloatingActionButton) findViewById(R.id.fabPurchaseProduct);
        fab.setOnClickListener(v -> purchaseProduct());

        textData = (TextView) findViewById(R.id.textData);

        billing = new Billing(this);
        billing.connect(this::fetchBillingState);
        billing.setOnPurchaseProductListener(new Billing.OnPurchaseProductListener() {
            @Override
            public void onProductPurchased(Purchase purchase) {
                if (verifyDeveloperPayload(purchase)) {
                    showSuccessPurchaseView(purchase);
                }
            }

            @Override
            public void onProductPurchaseError() {
                showErrorPurchaseView();
            }
        });

    }

    private void purchaseProduct() {
        billing.purchaseProduct(product);
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

    private boolean verifyDeveloperPayload(Purchase purchase) {
        return purchase.getPayload().equals(getString(R.string.billing_developer_payload));
    }

    private void checkInventory(Inventory inventory) {
        if (inventory != null) {
            Purchase purchase = inventory.isProductPurchased(Billing.PRODUCT_SKU);
            if (purchase != null) {
                showProductPurchasedView(purchase);
            } else {
                showPurchaseProductView(inventory.findProductBySku(Billing.PRODUCT_SKU));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
        billing.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        billing.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showSuccessPurchaseView(Purchase purchase) {
        AppSettingsManager.getInstance().setUserPro(true);
        fillPurchaseInfo(purchase);
        hideFab();

        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                "Yua are pro!", Snackbar.LENGTH_LONG).show();
    }


    private void showPurchaseProductView(Product product) {
        this.product = product;
        fillProductInfo(product);
        showFab();
    }

    private void showProductPurchasedView(Purchase purchase) {
        fillPurchaseInfo(purchase);
        hideFab();
    }

    private void fillPurchaseInfo(Purchase purchase) {
        textData.setText("");
        textData.setText(purchase.toString());
    }


    private void fillProductInfo(Product product) {
        textData.setText("");
        textData.setText(product.toString());
    }

    private void showErrorPurchaseView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                "Purchase Error", Snackbar.LENGTH_LONG).show();
    }

    private void showBillingNotSupportedView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                "Billing not support", Snackbar.LENGTH_LONG).show();
    }

    private void showConnectedErrorView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                "Billing connecting error", Snackbar.LENGTH_LONG).show();
    }

    private void showThrowable(Throwable throwable) {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                throwable.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void showFab() {
        fab.setVisibility(View.VISIBLE);
    }

    private void hideFab() {
        fab.setVisibility(View.GONE);
    }

}
