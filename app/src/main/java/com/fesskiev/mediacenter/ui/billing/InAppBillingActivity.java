package com.fesskiev.mediacenter.ui.billing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.billing.Billing;
import com.fesskiev.mediacenter.utils.billing.Product;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InAppBillingActivity extends AppCompatActivity {

    private Subscription subscription;
    private Billing billing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> getPurchaseList());

        billing = new Billing(this);
        billing.connect();

    }

    private void getPurchaseList() {
        subscription = billing.getPurchaseResult().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(inAppProducts -> {
                    for (Product product : inAppProducts) {
                        Log.e("product", "[pr]: " + product.toString());
                    }
                })
                .subscribe(productList -> {
                    Log.e("product", "try purchase product");
                }, Throwable::printStackTrace);
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
}
