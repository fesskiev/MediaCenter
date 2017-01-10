package com.fesskiev.mediacenter.data.source.remote;


import android.app.Activity;
import android.support.design.widget.Snackbar;

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


    public static ErrorHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ErrorHelper();
        }
        return INSTANCE;
    }

    private ErrorHelper() {

    }

    public void createErrorSnackBar(Activity activity, Throwable throwable, OnErrorHandlerListener listener) {
        int stringRes = -1;
        if (throwable instanceof RetrofitException) {
            RetrofitException exception = (RetrofitException) throwable;
            switch (exception.getKind()) {
                case HTTP:
                    stringRes = R.string.snackbar_server_error;
                    break;
                case NETWORK:
                    if (!NetworkHelper.isConnected(activity)) {
                        stringRes = R.string.snackbar_internet_disable_error;
                    } else {
                        stringRes = R.string.snackbar_internet_connection_error;
                    }
                    break;
                case UNEXPECTED:
                    stringRes = R.string.snackbar_unexpected_error;
                    break;
            }

            if (listener != null) {
                createSnackBarWithListener(activity, stringRes, listener);
            } else {
                createSnackBar(activity, stringRes);
            }
        }
    }


    private void createSnackBarWithListener(Activity activity, int stringRes, OnErrorHandlerListener listener) {
        Utils.showInternetErrorCustomSnackbar(activity.findViewById(R.id.bottom_navigation),
                activity.getApplicationContext(),
                stringRes, Snackbar.LENGTH_INDEFINITE)
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

    private void createSnackBar(Activity activity, int stringRes) {
        Utils.showInternetErrorCustomSnackbar(activity.findViewById(R.id.bottom_navigation),
                activity.getApplicationContext(), stringRes, Snackbar.LENGTH_INDEFINITE).show();
    }

}
