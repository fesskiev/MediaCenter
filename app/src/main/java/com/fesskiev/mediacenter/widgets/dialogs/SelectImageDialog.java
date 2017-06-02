package com.fesskiev.mediacenter.widgets.dialogs;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.search.Image;

import java.util.ArrayList;

public class SelectImageDialog extends DialogFragment {

    private static final String EXTRA_IMAGES = "com.fesskiev.player.EXTRA_IMAGES";

    public interface OnSelectedImageListener {

        void onSelectedImage(Image image);
    }

    public static SelectImageDialog newInstance(ArrayList<Image> images) {
        SelectImageDialog dialog = new SelectImageDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_IMAGES, images);
        dialog.setArguments(args);
        return dialog;
    }

    private OnSelectedImageListener listener;
    private ArrayList<Image> images;
    private Image selectedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        if (savedInstanceState != null) {
            images = savedInstanceState.getParcelableArrayList(EXTRA_IMAGES);
        } else {
            images = getArguments().getParcelableArrayList(EXTRA_IMAGES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXTRA_IMAGES, images);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_selected_image, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.saveCover).setOnClickListener(v -> changeListener());
        if (images != null) {
            RadioGroup radioGroup = new RadioGroup(getContext());
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> selectedImage = images.get(checkedId));

            radioGroup.setOrientation(RadioGroup.VERTICAL);
            for (int i = 0; i < images.size() - 1; i++) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                String size = images.get(i).getSize();
                if (!size.isEmpty()) {
                    radioButton.setText(size);
                    radioButton.setId(i);
                }
                radioGroup.addView(radioButton);

            }
            LinearLayout ll = (LinearLayout) view.findViewById(R.id.radioButtonsRoot);
            ll.addView(radioGroup);
        }
    }

    private void changeListener() {
        if (listener != null && selectedImage != null) {
            listener.onSelectedImage(selectedImage);
            dismiss();
        }
    }

    public void setOnSelectedImageListener(OnSelectedImageListener l) {
        this.listener = l;
    }
}
