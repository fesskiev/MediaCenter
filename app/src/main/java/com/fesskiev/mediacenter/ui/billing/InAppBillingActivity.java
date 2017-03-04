package com.fesskiev.mediacenter.ui.billing;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.fesskiev.mediacenter.R;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InAppBillingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BUY = 1234;

    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    public static final int PURCHASE_STATUS_PURCHASED = 0;
    public static final int PURCHASE_STATUS_CANCELLED = 1;
    public static final int PURCHASE_STATUS_REFUNDED = 2;

    private IInAppBillingService inAppBillingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> getPurchaseResult());


        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void getPurchaseResult() {
        Observable.just(getInAppPurchases("subs", "com.fesskiev.mediacenter.trial"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(inAppProducts -> {
                    for (InAppProduct product : inAppProducts) {
                        Log.e("product", "[pr]: " + product.toString());
                    }
                })
                .subscribe(inAppProduct -> {
                    Log.e("product", "try purchase product");
                    purchaseProduct(inAppProduct.get(0));
                }, Throwable::printStackTrace);

    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            inAppBillingService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            inAppBillingService = null;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }


    private List<InAppProduct> getInAppPurchases(String type, String... productIds) {
        ArrayList<String> skuList = new ArrayList<>(Arrays.asList(productIds));
        Bundle query = new Bundle();
        query.putStringArrayList("ITEM_ID_LIST", skuList);

        List<InAppProduct> result = null;
        try {

            Bundle skuDetails = inAppBillingService.getSkuDetails(3, getPackageName(), type, query);

            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
            result = new ArrayList<>();
            for (String responseItem : responseList) {

                Gson gson = new Gson();
                InAppProduct product = gson.fromJson(responseItem, InAppProduct.class);

                result.add(product);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void purchaseProduct(InAppProduct product) {
        String sku = product.getSku();
        String type = product.getType();
        // сюда вы можете добавить произвольные данные
        // потом вы сможете получить их вместе с покупкой
        String developerPayload = "12345";

        try {
            Bundle buyIntentBundle = inAppBillingService.getBuyIntent(3, getPackageName(),
                    sku, type, developerPayload);
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    REQUEST_CODE_BUY, new Intent(), 0, 0, 0, null);

        } catch (RemoteException | IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BUY) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", -1);
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                // можете проверить цифровую подпись
                readPurchase(purchaseData);
            } else {
                // обрабатываем ответ
            }
        }
    }

    private void readPurchase(String purchaseData) {
        Log.e("product", "[purchase]: " + purchaseData);
    }


    private class InAppProduct {

        @SerializedName("productId")
        @Expose
        private String productId;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("price")
        @Expose
        private String price;
        @SerializedName("price_amount_micros")
        @Expose
        private int priceAmountMicros;
        @SerializedName("price_currency_code")
        @Expose
        private String priceCurrencyCode;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("description")
        @Expose
        private String description;

        @Override
        public String toString() {
            return "InAppProduct{" +
                    "productId='" + productId + '\'' +
                    ", type='" + type + '\'' +
                    ", price='" + price + '\'' +
                    ", priceAmountMicros=" + priceAmountMicros +
                    ", priceCurrencyCode='" + priceCurrencyCode + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }

        public String getSku() {
            return productId;
        }

        String getType() {
            return type;
        }
    }
}
