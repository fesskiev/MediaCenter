package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AudioFolderDetailsDialog extends DialogFragment {

    private static final String DETAIL_AUDIO_FOLDER = "com.fesskiev.mediacenter.DETAIL_AUDIO_FOLDER";

    public static AudioFolderDetailsDialog newInstance(AudioFolder audioFolder) {
        AudioFolderDetailsDialog dialog = new AudioFolderDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_AUDIO_FOLDER, audioFolder);
        dialog.setArguments(args);
        return dialog;
    }

    private AudioFolder audioFolder;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        audioFolder = getArguments().getParcelable(DETAIL_AUDIO_FOLDER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_audio_folder_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(audioFolder != null){
            ImageView cover = (ImageView) view.findViewById(R.id.folderCover);
            BitmapHelper.getInstance().loadAudioFolderArtwork(audioFolder, cover);

            TextView folderName = (TextView) view.findViewById(R.id.folderName);
            folderName.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_name),
                    audioFolder.folderName));
            folderName.setSelected(true);

            TextView folderPath = (TextView) view.findViewById(R.id.folderPath);
            folderPath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                    audioFolder.folderPath.getAbsolutePath()));
            folderPath.setSelected(true);

            TextView folderSize = (TextView) view.findViewById(R.id.folderSize);
            folderSize.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                    Utils.humanReadableByteCount(audioFolder.size, false)));

            TextView folderLength = (TextView) view.findViewById(R.id.folderLength);
            folderLength.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                    Utils.getDurationString(audioFolder.length)));

            TextView folderTrackCount = (TextView) view.findViewById(R.id.folderTrackCount);
            folderTrackCount.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_count),
                    audioFolder.trackCount));

            TextView folderTimestamp = (TextView) view.findViewById(R.id.folderTimestamp);
            folderTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                    new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(audioFolder.timestamp))));

        }
    }
}
