package com.fesskiev.mediacenter.utils.converter;

public interface ILoadCallback {
    
    void onSuccess();
    
    void onFailure(Exception error);
    
}