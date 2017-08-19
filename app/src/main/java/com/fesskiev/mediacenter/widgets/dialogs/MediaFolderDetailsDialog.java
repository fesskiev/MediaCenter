package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFolder;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public abstract class MediaFolderDetailsDialog extends DialogFragment {

    public abstract void fillFolderData();

    public abstract void fetchFolderFiles();

    public interface OnMediaFolderDetailsDialogListener {

        void onRefreshFolder();
    }

    protected static final String DETAIL_MEDIA_FOLDER = "com.fesskiev.player.DETAIL_MEDIA_FOLDER";


    protected OnMediaFolderDetailsDialogListener listener;

    protected ImageView cover;
    protected TextView folderSizeText;
    protected TextView folderLengthText;
    protected TextView folderTrackCountText;
    protected TextView folderName;
    protected TextView folderPath;
    protected TextView folderTimestamp;
    protected CheckBox hideFolder;

    protected Subscription subscription;
    protected DataRepository repository;

    protected long folderSize = 0L;
    protected long folderLength = 0L;
    protected int folderTrackCount = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        repository = MediaApplication.getInstance().getRepository();
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

        cover = (ImageView) view.findViewById(R.id.folderCover);
        folderName = (TextView) view.findViewById(R.id.folderName);
        folderPath = (TextView) view.findViewById(R.id.folderPath);
        folderSizeText = (TextView) view.findViewById(R.id.folderSize);
        folderLengthText = (TextView) view.findViewById(R.id.folderLength);
        folderTrackCountText = (TextView) view.findViewById(R.id.folderTrackCount);
        folderTimestamp = (TextView) view.findViewById(R.id.folderTimestamp);
        hideFolder = (CheckBox) view.findViewById(R.id.hiddenFolderCheckBox);
        hideFolder.setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));

        fillFolderData();
        fetchFolderFiles();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
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
