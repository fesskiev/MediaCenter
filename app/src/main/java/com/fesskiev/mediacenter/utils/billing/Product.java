package com.fesskiev.mediacenter.utils.billing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("productId")
    @Expose
    private String sku;

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
                "productId='" + sku + '\'' +
                ", type='" + type + '\'' +
                ", price='" + price + '\'' +
                ", priceAmountMicros=" + priceAmountMicros +
                ", priceCurrencyCode='" + priceCurrencyCode + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getSku() {return sku;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public int getPriceAmountMicros() {
        return priceAmountMicros;
    }

    public String getPriceCurrencyCode() {
        return priceCurrencyCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
