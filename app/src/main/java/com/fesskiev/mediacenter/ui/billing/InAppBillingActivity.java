package com.fesskiev.mediacenter.ui.billing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

    private FloatingActionButton purchaseButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height_middle, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        purchaseButton = (FloatingActionButton) findViewById(R.id.fabPurchaseProduct);
        purchaseButton.setOnClickListener(v -> purchaseProduct());

        billing = new Billing(this);
        billing.connect(this::fetchBillingState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        showProgressBar();
        billing.setOnPurchaseProductListener(new Billing.OnPurchaseProductListener() {
            @Override
            public void onProductPurchased(Purchase purchase) {
                if (verifyDeveloperPayload(purchase)) {
                    hideProgressBar();
                    showSuccessPurchaseView();
                }
            }

            @Override
            public void onProductPurchaseError() {
                hideProgressBar();
                showErrorPurchaseView();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        billing.disconnect();
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
            if (purchase == null) {
                showPurchaseProductView(inventory.findProductBySku(Billing.PRODUCT_SKU));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        billing.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSuccessPurchaseView() {
        AppSettingsManager.getInstance().setUserPro(true);
        hideFab();
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                getString(R.string.ad_mob_remove_success), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.snack_exit_action), v -> finishWithResult())
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        finishWithResult();
                    }
                }).show();
    }

    private void finishWithResult() {
        finish();
    }


    private void showPurchaseProductView(Product product) {
        this.product = product;
        fillProductInfo(product);
        showFab();
    }


    private void fillProductInfo(Product product) {
        TextView amount = (TextView) findViewById(R.id.productAmount);
        TextView title = (TextView) findViewById(R.id.productTitle);
        TextView description = (TextView) findViewById(R.id.productDesc);

        amount.setText(String.format("%s %s", getString(R.string.product_price), product.getPrice()));
        title.setText(product.getTitle());
        description.setText(product.getDescription());

    }

    private void showErrorPurchaseView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                getString(R.string.billing_error_purchase), Snackbar.LENGTH_LONG).show();
    }

    private void showBillingNotSupportedView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                getString(R.string.billing_error_not_support), Snackbar.LENGTH_LONG).show();
    }

    private void showConnectedErrorView() {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                getString(R.string.billing_error_connection), Snackbar.LENGTH_LONG).show();
    }

    private void showThrowable(Throwable throwable) {
        Utils.showCustomSnackbar(findViewById(R.id.billingRoot), getApplicationContext(),
                throwable.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showFab() {
        purchaseButton.setVisibility(View.VISIBLE);
    }

    private void hideFab() {
        purchaseButton.setVisibility(View.GONE);
    }

}
