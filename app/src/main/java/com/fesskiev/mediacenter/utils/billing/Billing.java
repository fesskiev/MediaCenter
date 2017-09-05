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
import com.fesskiev.mediacenter.R;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;;

public class Billing {

    public interface OnPurchaseProductListener {
        void onProductPurchased(Purchase purchase);

        void onProductPurchaseError();
    }

    public interface OnBillingConnectListener {
        void onConnectStateChanged(boolean connected);
    }

    private static final String EXTRA_RESPONSE = "RESPONSE_CODE";
    private static final String EXTRA_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String EXTRA_PURCHASE_SIGNATURE = "INAPP_DATA_SIGNATURE";

    private static final String BUNDLE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String BUNDLE_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String BUNDLE_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

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

    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";

    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBS = "subs";

    public static final String PRODUCT_SKU = "com.fesskiev.mediacenter.test.pro";

    enum State {

        INITIAL,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        FAILED
    }

    private OnBillingConnectListener connectListener;
    private OnPurchaseProductListener purchaseProductListener;
    private PlayStoreBroadcastReceiver playStoreBroadcastReceiver;

    private IInAppBillingService inAppBillingService;

    private WeakReference<Activity> activity;
    private State state;

    public Billing(Activity activity) {
        this.activity = new WeakReference<>(activity);
        this.playStoreBroadcastReceiver = new PlayStoreBroadcastReceiver(activity.getApplicationContext());
        this.state = State.INITIAL;
    }

    public boolean connect(OnBillingConnectListener l) {
        this.connectListener = l;
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
                connectError();
                return false;
            } catch (NullPointerException e) {
                // Meizu M3s phones might throw an NPE in Context#bindService (Attempt to read from field 'int com.android.server.am.ProcessRecord.uid' on a null object reference).
                // As in-app purchases don't work if connection to the billing service can't be
                // established let's not crash and allow users to continue using the app
                connectError();
                return false;
            }
        }
        return false;
    }

    private void connectError() {
        if (connectListener != null) {
            connectListener.onConnectStateChanged(false);
        }
    }

    private void connectSuccess() {
        if (connectListener != null) {
            connectListener.onConnectStateChanged(true);
        }
    }

    public void disconnect() {
        Activity act = activity.get();
        if (act != null) {
            act.unbindService(serviceConnection);
        }
        setState(State.DISCONNECTING);
    }

    public Observable<Boolean> isBillingSupported() {
        Activity act = activity.get();
        if (act != null) {
            try {
                int response = inAppBillingService.isBillingSupported(3, act.getPackageName(), ITEM_TYPE_INAPP);
                return Observable.just(response == BILLING_RESPONSE_RESULT_OK);
            } catch (RemoteException e) {
                e.printStackTrace();
                return Observable.just(false);
            }
        }
        return Observable.just(false);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_BUY) {
            final int responseCode = intent.getIntExtra(EXTRA_RESPONSE, -1);
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                final String data = intent.getStringExtra(EXTRA_PURCHASE_DATA);
                final String signature = intent.getStringExtra(EXTRA_PURCHASE_SIGNATURE);

                Activity act = activity.get();
                if (act != null) {
                    Gson gson = new Gson();
                    Purchase purchase = gson.fromJson(data, Purchase.class);
                    if (Security.verifyPurchase(act.getString(R.string.billing_api_key), data, signature)) {
                        purchaseSuccess(purchase);
                    }
                }
            } else {
                purchaseError();
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
            connectSuccess();
            setState(State.CONNECTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            inAppBillingService = null;
            connectError();
            setState(State.DISCONNECTING);
        }
    };

    public Observable<List<Product>> getProducts() {
        return Observable.just(getProducts(ITEM_TYPE_INAPP, PRODUCT_SKU));
    }

    private List<Product> getProducts(String type, String... productIds) {
        ArrayList<String> skuList = new ArrayList<>(Arrays.asList(productIds));
        Bundle query = new Bundle();
        query.putStringArrayList("ITEM_ID_LIST", skuList);

        List<Product> products = null;
        Activity act = activity.get();
        if (act != null) {
            try {

                Bundle skuDetails = inAppBillingService.getSkuDetails(3, act.getPackageName(), type, query);

                ArrayList<String> responseList = skuDetails.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
                if (responseList != null) {
                    products = new ArrayList<>();
                    Gson gson = new Gson();

                    for (String responseItem : responseList) {
                        Product product = gson.fromJson(responseItem, Product.class);
                        products.add(product);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return products;
    }

    public Observable<List<Purchase>> getPurchases() {
        return Observable.just(getPurchase(ITEM_TYPE_INAPP));
    }

    private List<Purchase> getPurchase(String type) {
        List<Purchase> purchases = null;
        Activity act = activity.get();
        if (act != null) {
            try {

                Bundle bundle = inAppBillingService.getPurchases(3, act.getPackageName(), type, null);

                List<String> datas = bundle.getStringArrayList(BUNDLE_DATA_LIST);
                List<String> signatures = bundle.getStringArrayList(BUNDLE_SIGNATURE_LIST);

                purchases = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < datas.size(); i++) {
                    String data = datas.get(i);
                    String signature = signatures.get(i);

                    if (Security.verifyPurchase(act.getString(R.string.billing_api_key), data, signature)) {
                        Purchase purchase = gson.fromJson(data, Purchase.class);
                        purchase.setSignature(signature);

                        purchases.add(purchase);
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return purchases;
    }

    public void purchaseProduct(Product product) {
        String sku = product.getSku();
        String type = product.getType();

        Activity act = activity.get();
        if (act != null) {
            try {
                String developerPayload = act.getString(R.string.billing_developer_payload);

                Bundle buyIntentBundle = inAppBillingService.getBuyIntent(3, act.getPackageName(),
                        sku, type, developerPayload);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);

                act.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_BUY, new Intent(), 0, 0, 0, null);

            } catch (RemoteException | IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnPurchaseProductListener(OnPurchaseProductListener l) {
        this.purchaseProductListener = l;
    }

    private void purchaseSuccess(Purchase purchase) {
        if (purchaseProductListener != null) {
            purchaseProductListener.onProductPurchased(purchase);
        }
    }

    private void purchaseError() {
        if (purchaseProductListener != null) {
            purchaseProductListener.onProductPurchaseError();
        }
    }
}
