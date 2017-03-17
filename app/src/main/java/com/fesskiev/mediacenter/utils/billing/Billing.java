package com.fesskiev.mediacenter.utils.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

public class Billing {

    private static final String EXTRA_RESPONSE = "RESPONSE_CODE";
    private static final String EXTRA_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String EXTRA_PURCHASE_SIGNATURE = "INAPP_DATA_SIGNATURE";
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

    enum State {

        INITIAL,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        FAILED
    }

    private PlayStoreBroadcastReceiver playStoreBroadcastReceiver;

    private IInAppBillingService inAppBillingService;

    private WeakReference<Activity> activity;
    private State state;

    public Billing(Activity activity) {
        this.activity = new WeakReference<>(activity);
        this.playStoreBroadcastReceiver = new PlayStoreBroadcastReceiver(activity.getApplicationContext());
        this.state = State.INITIAL;
    }

    public boolean connect() {
        Activity act = activity.get();
        if (act != null) {
            try {
                final Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
                intent.setPackage("com.android.vending");
                return act.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            } catch (IllegalArgumentException e) {
                // some devices throw IllegalArgumentException (Service Intent must be explicit)
                // even though we set package name explicitly. Let's not crash the app and catch
                // such exceptions here, the billing on such devices will not work.
                return false;
            } catch (NullPointerException e) {
                // Meizu M3s phones might throw an NPE in Context#bindService (Attempt to read from field 'int com.android.server.am.ProcessRecord.uid' on a null object reference).
                // As in-app purchases don't work if connection to the billing service can't be
                // established let's not crash and allow users to continue using the app
                return false;
            }
        }
        return false;
    }

    public void disconnect() {
        Activity act = activity.get();
        if (act != null) {
            act.unbindService(serviceConnection);
        }
        setState(State.DISCONNECTING);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_BUY) {
            final int responseCode = intent.getIntExtra(EXTRA_RESPONSE, -1);
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                final String data = intent.getStringExtra(EXTRA_PURCHASE_DATA);
                final String signature = intent.getStringExtra(EXTRA_PURCHASE_SIGNATURE);

                Gson gson = new Gson();
                Purchase purchase = gson.fromJson(data, Purchase.class);

                //TODO add my public key
                if (Security.verifyPurchase("", data, signature)) {

                }
            } else {
                // обрабатываем ответ
            }
        }
    }


    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        switch (state) {
            case DISCONNECTING:
                playStoreBroadcastReceiver.removeOnPlayStoreListener();
                break;
            case CONNECTED:
                playStoreBroadcastReceiver.setOnPlayStoreListener(playStoreListener);
                break;
            case FAILED:
                break;
        }

    }

    private final OnPlayStoreListener playStoreListener = () -> {

    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            inAppBillingService = IInAppBillingService.Stub.asInterface(service);
            setState(State.CONNECTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            inAppBillingService = null;
            setState(State.DISCONNECTING);
        }
    };

    public Observable<List<Product>> getPurchaseResult() {
        return Observable.just(getPurchases("subs", "com.fesskiev.mediacenter.trial"));
    }

    private List<Product> getPurchases(String type, String... productIds) {
        ArrayList<String> skuList = new ArrayList<>(Arrays.asList(productIds));
        Bundle query = new Bundle();
        query.putStringArrayList("ITEM_ID_LIST", skuList);

        List<Product> result = null;
        Activity act = activity.get();
        if (act != null) {
            try {

                Bundle skuDetails = inAppBillingService.getSkuDetails(3, act.getPackageName(), type, query);

                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                if (responseList != null) {
                    result = new ArrayList<>();
                    for (String responseItem : responseList) {

                        Gson gson = new Gson();
                        Product product = gson.fromJson(responseItem, Product.class);

                        result.add(product);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void purchaseProduct(Product product) {
        String sku = product.getSku();
        String type = product.getType();
        // сюда вы можете добавить произвольные данные
        // потом вы сможете получить их вместе с покупкой
        String developerPayload = "12345";

        Activity act = activity.get();
        if (act != null) {
            try {
                Bundle buyIntentBundle = inAppBillingService.getBuyIntent(3, act.getPackageName(),
                        sku, type, developerPayload);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                act.startIntentSenderForResult(pendingIntent.getIntentSender(),
                        REQUEST_CODE_BUY, new Intent(), 0, 0, 0, null);

            } catch (RemoteException | IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }
}
