package com.fesskiev.mediacenter.data.source.remote.retrofit;


import android.content.Context;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.NetworkHelper;
import com.fesskiev.mediacenter.data.source.remote.exceptions.RetrofitException;


public class RetrofitErrorHelper {

    public static String getErrorDescription(Context context, Throwable throwable) {
        int stringRes;
        if (throwable instanceof RetrofitException) {
            RetrofitException exception = (RetrofitException) throwable;
            switch (exception.getKind()) {
                case HTTP:
                    stringRes = R.string.snackbar_server_error;
                    int code = exception.getResponse().code();
                    if (code == 401) {
                        stringRes = R.string.snackbar_auth_error;
                    }
                    break;
                case NETWORK:
                    if (!NetworkHelper.isConnected(context)) {
                        stringRes = R.string.snackbar_internet_disable_error;
                    } else {
                        stringRes = R.string.snackbar_internet_connection_error;
                    }
                    break;
                case UNEXPECTED:
                    stringRes = R.string.snackbar_unexpected_error;
                    break;
                default:
                    stringRes = R.string.snackbar_unexpected_error;
                    break;
            }

        } else {
            stringRes = R.string.snackbar_unexpected_error;
        }
        return context.getString(stringRes);
    }
}
