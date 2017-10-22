package com.fesskiev.mediacenter.ui.wear;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.WearHelper;


public class WearFragment extends Fragment {

    public static WearFragment newInstance() {
        return new WearFragment();
    }

    private Button installAppButton;
    private Button openAppButton;
    private TextView connectionState;

    private WearHelper wearHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wear, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        openAppButton = view.findViewById(R.id.openAppButton);
        openAppButton.setOnClickListener(v -> wearHelper.startWearApp());

        installAppButton = view.findViewById(R.id.installAppButton);
        installAppButton.setOnClickListener(v -> wearHelper.openPlayStoreOnWearDevicesWithoutApp());

        connectionState = view.findViewById(R.id.connectionStatusText);

        wearHelper = new WearHelper(getContext().getApplicationContext());
        wearHelper.setOnWearConnectionListener(new WearHelper.OnWearConnectionListener() {
            @Override
            public void onNoDeviceConnected() {
                noConnected();
            }

            @Override
            public void onWithoutApp() {
                withoutApp();
            }

            @Override
            public void onSomeDeviceWithApp() {
                someDeviceWithApp();
            }

            @Override
            public void onAllDeviceWithApp() {
                openApp();
            }
        });
        wearHelper.setPlayStoreResultListener(new WearHelper.OnPlayStoreResultListener() {
            @Override
            public void onRequestSuccessful() {
                showPlayStoreSuccess();
            }

            @Override
            public void onRequestFailed() {
                showPlayStoreFailed();
            }
        });

        if (wearHelper.isAvailable()) {
            wearHelper.connect();
        } else {
            googlePlayServicesNotAvailable();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wearHelper.disconnect();
    }

    private void googlePlayServicesNotAvailable() {
        openAppButton.setVisibility(View.INVISIBLE);
        installAppButton.setVisibility(View.INVISIBLE);
        connectionState.setText(getString(R.string.wear_not_available));
        connectionState.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_wear_no_connected, 0, 0);
    }

    private void someDeviceWithApp() {
        openAppButton.setVisibility(View.INVISIBLE);
        installAppButton.setVisibility(View.VISIBLE);
        connectionState.setText(getString(R.string.wear_some_devices));
        connectionState.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_wear_get_app, 0, 0);
    }

    private void withoutApp() {
        openAppButton.setVisibility(View.INVISIBLE);
        installAppButton.setVisibility(View.VISIBLE);
        connectionState.setText(getString(R.string.wear_without_app));
        connectionState.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_wear_get_app, 0, 0);
    }

    private void noConnected() {
        openAppButton.setVisibility(View.INVISIBLE);
        installAppButton.setVisibility(View.INVISIBLE);
        connectionState.setText(getString(R.string.wear_no_connected));
        connectionState.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_wear_no_connected, 0, 0);
    }

    private void openApp() {
        openAppButton.setVisibility(View.VISIBLE);
        installAppButton.setVisibility(View.INVISIBLE);
        connectionState.setText(getString(R.string.wear_open_app));
        connectionState.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_wear_connected, 0, 0);
    }

    private void showPlayStoreFailed() {
        Utils.showCustomSnackbar(getView(), getActivity().getApplicationContext(),
                getString(R.string.wear_play_store_request_success), Snackbar.LENGTH_LONG)
                .show();
    }

    private void showPlayStoreSuccess() {
        Utils.showCustomSnackbar(getView(), getActivity().getApplicationContext(),
                getString(R.string.wear_play_store_request_failed), Snackbar.LENGTH_LONG)
                .show();
    }
}
