package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;

public class SimpleDialog extends DialogFragment implements View.OnClickListener {

    private static final String TITLE_TEXT = "com.fesskiev.player.TITLE_TEXT";
    private static final String MESSAGE_TEXT = "com.fesskiev.player.MESSAGE_TEXT";
    private static final String RES_ID = "com.fesskiev.player.RES_ID";


    public static SimpleDialog newInstance(String title, String message, int resId) {
        SimpleDialog dialog = new SimpleDialog();
        Bundle args = new Bundle();
        args.putString(TITLE_TEXT, title);
        args.putString(MESSAGE_TEXT, message);
        args.putInt(RES_ID, resId);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnDialogPositiveListener {

        void onPositiveClick();

    }

    public interface OnDialogNegativeListener {

        void onNegativeClick();
    }

    private OnDialogPositiveListener positiveListener;
    private OnDialogNegativeListener negativeListener;

    private String title;
    private String message;
    private int resId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        title = getArguments().getString(TITLE_TEXT);
        message = getArguments().getString(MESSAGE_TEXT);
        resId = getArguments().getInt(RES_ID, -1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_simple, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleText = view.findViewById(R.id.dialogTitle);
        if (title != null && !TextUtils.isEmpty(title)) {
            titleText.setText(title);
        }
        TextView messageText = view.findViewById(R.id.dialogMessage);
        if (message != null && !TextUtils.isEmpty(message)) {
            messageText.setText(message);
        }
        ImageView icon = view.findViewById(R.id.dialogIcon);
        if (resId != -1) {
            icon.setImageResource(resId);
        }

        Button[] buttons = new Button[]{
                view.findViewById(R.id.buttonPositiveConfirm),
                view.findViewById(R.id.buttonNegativeConfirm)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPositiveConfirm:
                confirmPositive();
                break;
            case R.id.buttonNegativeConfirm:
                confirmNegative();
                break;
        }
        dismiss();
    }

    private void confirmNegative() {
        if (negativeListener != null) {
            negativeListener.onNegativeClick();
        }
    }

    private void confirmPositive() {
        if (positiveListener != null) {
            positiveListener.onPositiveClick();
        }
    }

    public void setPositiveListener(OnDialogPositiveListener positiveListener) {
        this.positiveListener = positiveListener;
    }

    public void setNegativeListener(OnDialogNegativeListener negativeListener) {
        this.negativeListener = negativeListener;
    }
}
