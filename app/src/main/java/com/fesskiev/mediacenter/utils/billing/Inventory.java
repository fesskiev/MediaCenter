
package com.fesskiev.mediacenter.utils.billing;


import java.util.List;

public class Inventory {

    private List<Product> products;
    private List<Purchase> purchases;

    public Inventory(List<Product> products, List<Purchase> purchases) {
        this.products = products;
        this.purchases = purchases;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public Purchase isProductPurchased(String sku) {
        for (Purchase purchase : purchases) {
            if (purchase.getSku().equals(sku)) {
                return purchase;
            }
        }
        return null;
    }

    public Product findProductBySku(String sku) {
        for (Product product : products) {
            if (product.getSku().equals(sku)) {
                return product;
            }
        }
        return null;
    }
}
