package com.fesskiev.player.data.source.local.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MediaCenterDatabase";
    private static final int DATABASE_VERSION = 1;

    public static final String AUDIO_FOLDERS_TABLE_NAME = "AudioFolders";
    public static final String AUDIO_TRACKS_TABLE_NAME = "AudioTracks";
    public static final String VIDEO_FILES_TABLE_NAME = "VideoFiles";

    public static final String ID = "ID";

    /**
     * video file constants
     */
    public static final String VIDEO_FILE_PATH = "VideoFilePath";
    public static final String VIDEO_FRAME_PATH = "VideoFramePath";
    public static final String VIDEO_DESCRIPTION = "VideoDescription";
    public static final String VIDEO_IN_PLAY_LIST = "VideoInPlayList";
    public static final String VIDEO_SELECTED = "VideoSelected";

    /**
     * audio folder constants
     */
    public static final String FOLDER_PATH = "FolderPath";
    public static final String FOLDER_COVER = "FolderCover";
    public static final String FOLDER_NAME = "FolderName";
    public static final String FOLDER_INDEX = "FolderIndex";
    public static final String FOLDER_SELECTED = "FolderSelected";

    /**
     * audio file constants
     */
    public static final String TRACK_PATH = "TrackPath";
    public static final String TRACK_ARTIST = "TrackArtist";
    public static final String TRACK_TITLE = "TrackTitle";
    public static final String TRACK_ALBUM = "TrackAlbum";
    public static final String TRACK_GENRE = "TrackGenre";
    public static final String TRACK_BITRATE = "TrackBitrate";
    public static final String TRACK_SAMPLE_RATE = "TrackSampleRate";
    public static final String TRACK_COVER = "TrackCover";
    public static final String TRACK_NUMBER = "TrackNumber";
    public static final String TRACK_LENGTH = "TrackLength";
    public static final String TRACK_IN_PLAY_LIST = "TrackInPlayList";
    public static final String TRACK_SELECTED = "TrackSelected";


    private static final String KEY_TYPE = "TEXT NOT NULL";
    private static final String TEXT_TYPE = "TEXT";
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String REAL_TYPE = "REAL";

    public static final String CREATE_VIDEO_FILES_TABLE_SQL = "CREATE TABLE" + " " +
            VIDEO_FILES_TABLE_NAME + " " +
            "(" + " " +
            ID + " " + KEY_TYPE + " ," +
            VIDEO_FILE_PATH + " " + TEXT_TYPE + " ," +
            VIDEO_FRAME_PATH + " " + TEXT_TYPE + " ," +
            VIDEO_DESCRIPTION + " " + TEXT_TYPE + " ," +
            VIDEO_SELECTED + " " + INTEGER_TYPE + " ," +
            VIDEO_IN_PLAY_LIST + " " + INTEGER_TYPE +
            ")";


    public static final String CREATE_AUDIO_FOLDERS_TABLE_SQL = "CREATE TABLE" + " " +
            AUDIO_FOLDERS_TABLE_NAME + " " +
            "(" + " " +
            ID + " " + KEY_TYPE + " ," +
            FOLDER_PATH + " " + TEXT_TYPE + " ," +
            FOLDER_NAME + " " + TEXT_TYPE + " ," +
            FOLDER_COVER + " " + TEXT_TYPE + " ," +
            FOLDER_SELECTED + " " + INTEGER_TYPE + " ," +
            FOLDER_INDEX + " " + INTEGER_TYPE +
            ")";

    public static final String CREATE_AUDIO_TRACKS_TABLE_SQL = "CREATE TABLE" + " " +
            AUDIO_TRACKS_TABLE_NAME + " " +
            "(" + " " +
            ID + " " + KEY_TYPE + " ," +
            TRACK_PATH + " " + TEXT_TYPE + " ," +
            TRACK_ARTIST + " " + TEXT_TYPE + " ," +
            TRACK_TITLE + " " + TEXT_TYPE + " ," +
            TRACK_ALBUM + " " + TEXT_TYPE + " ," +
            TRACK_GENRE + " " + TEXT_TYPE + " ," +
            TRACK_BITRATE + " " + INTEGER_TYPE + " ," +
            TRACK_SAMPLE_RATE + " " + INTEGER_TYPE + " ," +
            TRACK_NUMBER + " " + INTEGER_TYPE + " ," +
            TRACK_LENGTH + " " + REAL_TYPE + " ," +
            TRACK_IN_PLAY_LIST + " " + INTEGER_TYPE + " ," +
            TRACK_SELECTED + " " + INTEGER_TYPE + " ," +
            TRACK_COVER + " " + TEXT_TYPE +
            ")";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AUDIO_FOLDERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AUDIO_TRACKS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VIDEO_FILES_TABLE_NAME);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_AUDIO_FOLDERS_TABLE_SQL);
        db.execSQL(CREATE_AUDIO_TRACKS_TABLE_SQL);
        db.execSQL(CREATE_VIDEO_FILES_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }
}
