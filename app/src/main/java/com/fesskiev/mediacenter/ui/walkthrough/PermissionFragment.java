package com.fesskiev.mediacenter.ui.walkthrough;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;


public class PermissionFragment extends Fragment implements View.OnClickListener {

    public static PermissionFragment newInstance() {
        return new PermissionFragment();
    }

    private static final int PERMISSION_REQ = 0;

    private TextView permissionText;
    private Button[] buttons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        permissionText = (TextView) view.findViewById(R.id.permissionText);

        buttons = new Button[]{
                (Button) view.findViewById(R.id.buttonGranted),
                (Button) view.findViewById(R.id.buttonCancel)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        if (checkPermissions()) {
            showSuccessPermissions();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonGranted:
                requestPermissions();
                break;
            case R.id.buttonCancel:
                permissionsDenied();
                break;
        }
    }

    public boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.MODIFY_AUDIO_SETTINGS) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.RECORD_AUDIO);
    }


    private void requestPermissions() {
        requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults != null && grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showSuccessPermissions();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale =
                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (showRationale) {
                            permissionsDenied();
                        } else {
                            createExplanationPermissionDialog();
                        }
                    }
                }
                break;
            }
        }
    }

    private void showSuccessPermissions() {
        permissionText.setText(getString(R.string.permission_granted));
        permissionText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_walk_through_ok, 0, 0);
        permissionText.setCompoundDrawablePadding((int) Utils.dipToPixels(getContext().getApplicationContext(), 8));

        hideButtons();
        notifyPermissionGranted();
    }

    private void notifyPermissionGranted() {
        WalkthroughFragment walkthroughFragment = (WalkthroughFragment) getFragmentManager().
                findFragmentByTag(WalkthroughFragment.class.getName());
        if (walkthroughFragment != null) {
            walkthroughFragment.permissionGranted();
        }
    }

    private void hideButtons() {
        for (Button button : buttons) {
            button.setVisibility(View.GONE);
        }
    }

    private void permissionsDenied() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_permission_title), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_permission_button, v -> requestPermissions())
                .show();

    }

    private void createExplanationPermissionDialog() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_message), R.drawable.icon_permission_settings);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(() -> Utils.startSettingsActivity(getContext()));
        dialog.setNegativeListener(() -> getActivity().finish());

    }
}
