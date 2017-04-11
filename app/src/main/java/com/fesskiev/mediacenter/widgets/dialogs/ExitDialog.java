package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;


public class ExitDialog extends DialogFragment implements View.OnClickListener {

    private static final String EXIT_TEXT = "com.fesskiev.player.EXIT_TEXT";

    public interface OnExitListener {

        void onExitClick();
    }

    public static ExitDialog newInstance(String text) {
        ExitDialog dialog = new ExitDialog();
        Bundle args = new Bundle();
        args.putString(EXIT_TEXT, text);
        dialog.setArguments(args);
        return dialog;
    }

    private OnExitListener listener;
    private String exitText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        exitText = getArguments().getString(EXIT_TEXT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_exit, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.exitText)).setText(exitText);

        Button[] buttons = new Button[]{
                (Button) view.findViewById(R.id.buttonExitConfirm),
                (Button) view.findViewById(R.id.buttonExitDismiss)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonExitConfirm:
                confirmExit();
                break;
            case R.id.buttonExitDismiss:
                break;
        }
        dismiss();
    }

    private void confirmExit() {
        if (listener != null) {
            listener.onExitClick();
        }
    }

    public void setOnExitListener(OnExitListener l) {
        this.listener = l;
    }
}
