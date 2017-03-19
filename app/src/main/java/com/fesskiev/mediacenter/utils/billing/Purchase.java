package com.fesskiev.mediacenter.utils.billing;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Purchase {

    public enum State {
        PURCHASED(0),
        CANCELLED(1),
        REFUNDED(2),
        // billing v2 only
        EXPIRED(3);

        public final int id;

        State(int id) {
            this.id = id;
        }

        static State valueOf(int id) {
            switch (id) {
                case 0:
                    return PURCHASED;
                case 1:
                    return CANCELLED;
                case 2:
                    return REFUNDED;
                case 3:
                    return EXPIRED;
            }
            throw new IllegalArgumentException("Id=" + id + " is not supported");
        }
    }

    // the item's product identifier. Every item has a product ID, which you must specify
    // in the application's product list on the Google Play Developer Console
    @SerializedName("productId")
    @Expose
    private String sku;

    // a unique order identifier for the transaction. This identifier corresponds to the
    // Google Wallet Order ID
    @SerializedName("orderId")
    @Expose
    private String orderId;

    // the application package from which the purchase originated
    @SerializedName("packageName")
    @Expose
    private String packageName;

    // the time the product was purchased, in milliseconds since the epoch (Jan 1, 1970)
    @SerializedName("purchaseTime")
    @Expose
    private long time;

    // a developer-specified string that contains supplemental information about an order.
    // You can specify a value for this field when you make a getBuyIntent request
    @SerializedName("developerPayload")
    @Expose
    private String payload;

    // the purchase state of the order
    @SerializedName("purchaseState")
    @Expose
    private int state;

    // a token that uniquely identifies a purchase for a given item and user pair
    @SerializedName("purchaseToken")
    @Expose
    private String token;

    // Indicates whether the subscription renews automatically. If true, the subscription is active,
    // and will automatically renew on the next billing date. If false, indicates that the user has
    // canceled the subscription. The user has access to subscription content until the next billing
    // date and will lose access at that time unless they re-enable automatic renewal
    @SerializedName("autoRenewing")
    @Expose
    private boolean autoRenewing;

    private String signature;

    public Purchase() {

    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAutoRenewing() {
        return autoRenewing;
    }

    public void setAutoRenewing(boolean autoRenewing) {
        this.autoRenewing = autoRenewing;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "sku='" + sku + '\'' +
                ", orderId='" + orderId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", time=" + time +
                ", payload='" + payload + '\'' +
                ", state=" + state +
                ", token='" + token + '\'' +
                ", autoRenewing=" + autoRenewing +
                ", signature='" + signature + '\'' +
                '}';
    }
}
