package com.fesskiev.player.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.fesskiev.player.R;

public class PermissionDialog extends AlertDialog implements View.OnClickListener {

    public PermissionDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setCancelable(false);
    }

    public interface OnPermissionDialogListener {

        void onPermissionGranted();

        void onPermissionCancel();
    }


   private OnPermissionDialogListener listener;

    public static PermissionDialog newInstance(Context context) {
        return new PermissionDialog(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_permission);

        Button [] buttons = new Button[]{
                (Button) findViewById(R.id.buttonGranted),
                (Button) findViewById(R.id.buttonCancel)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
    }

    public void setOnPermissionDialogListener(OnPermissionDialogListener l) {
        this.listener = l;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            switch (v.getId()) {
                case R.id.buttonGranted:
                    listener.onPermissionGranted();
                    break;
                case R.id.buttonCancel:
                    listener.onPermissionCancel();
                    break;
            }
        }
    }
}
