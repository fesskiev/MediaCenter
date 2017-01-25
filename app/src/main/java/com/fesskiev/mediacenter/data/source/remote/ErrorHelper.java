package com.fesskiev.mediacenter.data.source.remote;


import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.NetworkHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.data.source.remote.exceptions.RetrofitException;


public class ErrorHelper {

    public interface OnErrorHandlerListener {

        void tryRequestAgain();

        void show(Snackbar snackbar);

        void hide(Snackbar snackbar);
    }

    private static ErrorHelper INSTANCE;
    private Context context;


    public static ErrorHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ErrorHelper();
        }
        return INSTANCE;
    }

    private ErrorHelper() {
        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void createErrorSnackBar(View view, Throwable throwable, OnErrorHandlerListener listener) {
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

            if (listener != null) {
                createSnackBarWithListener(view, stringRes, listener);
            } else {
                createSnackBar(view, stringRes);
            }
        }
    }


    private void createSnackBarWithListener(View view, int stringRes, OnErrorHandlerListener listener) {
        Utils.showInternetErrorCustomSnackbar(view, context, stringRes, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_error_try_again, v -> listener.tryRequestAgain())
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        listener.hide(snackbar);
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                        listener.show(snackbar);
                    }
                }).show();
    }

    private void createSnackBar(View view, int stringRes) {
        Utils.showInternetErrorCustomSnackbar(view, context, stringRes, Snackbar.LENGTH_LONG).show();
    }

}
