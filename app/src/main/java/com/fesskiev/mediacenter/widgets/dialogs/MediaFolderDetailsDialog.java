package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.RxUtils;

import io.reactivex.disposables.Disposable;


public abstract class MediaFolderDetailsDialog extends DialogFragment implements TextWatcher {

    public abstract void fillFolderData();

    public abstract void fetchFolderFiles();

    public abstract void folderNameChanged(String name);


    public interface OnMediaFolderDetailsDialogListener {

        void onRefreshFolder();
    }

    protected static final String DETAIL_MEDIA_FOLDER = "com.fesskiev.player.DETAIL_MEDIA_FOLDER";


    protected OnMediaFolderDetailsDialogListener listener;

    protected ImageView cover;
    protected TextView folderSizeText;
    protected TextView folderLengthText;
    protected TextView folderTrackCountText;
    protected EditText folderName;
    protected TextView folderPath;
    protected TextView folderTimestamp;
    protected CheckBox hideFolder;
    protected Button saveFolderNameButton;

    protected Disposable subscription;
    protected DataRepository repository;
    protected AudioPlayer audioPlayer;

    protected long folderSize = 0L;
    protected long folderLength = 0L;
    protected int folderTrackCount = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        repository = MediaApplication.getInstance().getRepository();
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
         *  bug! http://stackoverflow.com/questions/32784009/styling-custom-dialog-fragment-not-working?noredirect=1&lq=1
         */
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_audio_folder_details, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cover = view.findViewById(R.id.folderCover);

        folderName = view.findViewById(R.id.folderName);
        folderName.addTextChangedListener(this);

        folderPath = view.findViewById(R.id.folderPath);
        folderSizeText = view.findViewById(R.id.folderSize);
        folderLengthText = view.findViewById(R.id.folderLength);
        folderTrackCountText = view.findViewById(R.id.folderTrackCount);
        folderTimestamp = view.findViewById(R.id.folderTimestamp);
        hideFolder = view.findViewById(R.id.hiddenFolderCheckBox);
        hideFolder.setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));

        saveFolderNameButton = view.findViewById(R.id.saveFolderNameButton);


        fillFolderData();
        fetchFolderFiles();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String value = s.toString();
        if (!TextUtils.isEmpty(value)) {
            folderNameChanged(value);
        }
    }

    public void setOnMediaFolderDetailsDialogListener(OnMediaFolderDetailsDialogListener l) {
        this.listener = l;
    }

    protected void refreshCache() {
        if (listener != null) {
            listener.onRefreshFolder();
        }
    }
}
