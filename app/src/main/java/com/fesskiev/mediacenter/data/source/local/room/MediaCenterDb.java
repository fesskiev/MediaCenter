package com.fesskiev.mediacenter.data.source.local.room;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedAudioFile;
import com.fesskiev.mediacenter.data.model.SelectedAudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedVideoFile;
import com.fesskiev.mediacenter.data.model.SelectedVideoFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;

@Database(entities = {AudioFolder.class, AudioFile.class, VideoFolder.class,
        VideoFile.class, SelectedAudioFile.class, SelectedAudioFolder.class,
        SelectedVideoFolder.class, SelectedVideoFile.class}, version = 3)
@TypeConverters({PathConverter.class})
public abstract class MediaCenterDb extends RoomDatabase {

    abstract public MediaDao mediaDao();

}
