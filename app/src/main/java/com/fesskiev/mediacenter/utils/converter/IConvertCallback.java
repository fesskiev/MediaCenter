package com.fesskiev.mediacenter.utils.converter;

import java.io.File;

public interface IConvertCallback {
    
    void onSuccess(File convertedFile);
    
    void onFailure(Exception error);

}