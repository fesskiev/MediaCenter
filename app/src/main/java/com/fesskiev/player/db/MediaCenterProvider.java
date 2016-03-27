package com.fesskiev.player.db;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

public class MediaCenterProvider extends ContentProvider {

    public static final String TAG = MediaCenterProvider.class.getSimpleName();

    private static final String DATABASE_NAME = "MediaCenterDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String SCHEME = "content";

    private static final String AUTHORITY = "com.fesskiev.player.authority";

    private static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    private static final String AUDIO_FOLDERS_TABLE_NAME = "AudioFolders";

    private static final String AUDIO_TRACKS_TABLE_NAME = "AudioTracks";

    public static final Uri AUDIO_FOLDERS_TABLE_CONTENT_URI =
            Uri.withAppendedPath(CONTENT_URI, AUDIO_FOLDERS_TABLE_NAME);

    public static final Uri AUDIO_TRACKS_TABLE_CONTENT_URI =
            Uri.withAppendedPath(CONTENT_URI, AUDIO_TRACKS_TABLE_NAME);

    public static final String ID = "ID";
    /**
     * audio folder constants
     */
    public static final String FOLDER_PATH = "FolderPath";
    public static final String FOLDER_COVER = "FolderCover";
    public static final String FOLDER_NAME = "FolderName";

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

    private static final int AUDIO_FOLDERS_QUERY = 1;
    private static final int AUDIO_TRACK_QUERY = 2;
    private static final int INVALID_URI = -1;

    private static final String PRIMARY_KEY_TYPE = "TEXT NOT NULL PRIMARY KEY";
    private static final String TEXT_TYPE = "TEXT";
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String REAL_TYPE = "REAL";
    private static final String BLOB_TYPE = "BLOB";



    public static final String CREATE_AUDIO_FOLDERS_TABLE_SQL = "CREATE TABLE" + " " +
            AUDIO_FOLDERS_TABLE_NAME + " " +
            "(" + " " +
            ID + " " + PRIMARY_KEY_TYPE + " ," +
            FOLDER_PATH + " " + TEXT_TYPE + " ," +
            FOLDER_NAME + " " + TEXT_TYPE + " ," +
            FOLDER_COVER + " " + TEXT_TYPE +
            ")";

    public static final String CREATE_AUDIO_TRACKS_TABLE_SQL = "CREATE TABLE" + " " +
            AUDIO_TRACKS_TABLE_NAME + " " +
            "(" + " " +
            ID + " " + PRIMARY_KEY_TYPE + " ," +
            TRACK_PATH + " " + TEXT_TYPE + " ," +
            TRACK_ARTIST + " " + TEXT_TYPE + " ," +
            TRACK_TITLE + " " + TEXT_TYPE + " ," +
            TRACK_ALBUM + " " + TEXT_TYPE + " ," +
            TRACK_GENRE + " " + TEXT_TYPE + " ," +
            TRACK_BITRATE + " " + INTEGER_TYPE + " ," +
            TRACK_SAMPLE_RATE + " " + INTEGER_TYPE + " ," +
            TRACK_NUMBER + " " + INTEGER_TYPE + " ," +
            TRACK_LENGTH + " " + REAL_TYPE + " ," +
            TRACK_COVER + " " + BLOB_TYPE +
            ")";

    private SQLiteOpenHelper helper;

    // Stores the MIME types served by this provider
    private static final SparseArray<String> mimeTypes;

    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher uriMatcher;

    static {
        // Creates an object that associates content URIs with numeric codes
        uriMatcher = new UriMatcher(0);

         /*
         * Sets up an array that maps content URIs to MIME types, via a mapping between the
         * URIs and an integer code. These are custom MIME types that apply to tables and rows
         * in this particular provider.
         */
        mimeTypes = new SparseArray<>();

        uriMatcher.addURI(
                AUTHORITY,
                AUDIO_FOLDERS_TABLE_NAME,
                AUDIO_FOLDERS_QUERY);

        mimeTypes.put(
                AUDIO_FOLDERS_QUERY,
                "vnd.android.cursor.dir/vnd." +
                        AUTHORITY + "." +
                        AUDIO_FOLDERS_TABLE_NAME);

        uriMatcher.addURI(
                AUTHORITY,
                AUDIO_TRACKS_TABLE_NAME,
                AUDIO_TRACK_QUERY);

        mimeTypes.put(
                AUDIO_TRACK_QUERY,
                "vnd.android.cursor.dir/vnd." +
                        AUTHORITY + "." +
                        AUDIO_TRACKS_TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        helper = new MediaCenterProviderHelper(getContext());
        return true;
    }



    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor returnCursor;
        String tableName = null;
        switch (uriMatcher.match(uri)) {
            case AUDIO_FOLDERS_QUERY:
                tableName = AUDIO_FOLDERS_TABLE_NAME;
                break;
            case AUDIO_TRACK_QUERY:
                tableName = AUDIO_TRACKS_TABLE_NAME;
                break;
            case INVALID_URI:
                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);

        }

        returnCursor = db.query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return mimeTypes.get(uriMatcher.match(uri));
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = null;
        switch (uriMatcher.match(uri)) {
            case AUDIO_FOLDERS_QUERY:
                tableName = AUDIO_FOLDERS_TABLE_NAME;
                break;
            case AUDIO_TRACK_QUERY:
                tableName = AUDIO_TRACKS_TABLE_NAME;
                break;
            case INVALID_URI:
                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);

        }

        SQLiteDatabase localSQLiteDatabase = helper.getWritableDatabase();

        long id = localSQLiteDatabase.insert(
                tableName,
                null,
                values
        );

        if (id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.withAppendedPath(uri, Long.toString(id));
        } else {
            throw new SQLiteException("Insert error:" + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName = null;
        switch (uriMatcher.match(uri)) {
            case AUDIO_FOLDERS_QUERY:
                tableName = AUDIO_FOLDERS_TABLE_NAME;
                break;
            case AUDIO_TRACK_QUERY:
                tableName = AUDIO_TRACKS_TABLE_NAME;
                break;
            case INVALID_URI:
                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);

        }

        SQLiteDatabase localSQLiteDatabase = helper.getWritableDatabase();

        int rows = localSQLiteDatabase.delete(
                tableName,
                selection,
                selectionArgs);

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return rows;
        } else {
            throw new SQLiteException("Delete error:" + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = null;
        switch (uriMatcher.match(uri)) {
            case AUDIO_FOLDERS_QUERY:
                tableName = AUDIO_FOLDERS_TABLE_NAME;
                break;
            case AUDIO_TRACK_QUERY:
                tableName = AUDIO_TRACKS_TABLE_NAME;
                break;
            case INVALID_URI:
                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);

        }
        SQLiteDatabase localSQLiteDatabase = helper.getWritableDatabase();

        int rows = localSQLiteDatabase.update(
                tableName,
                values,
                selection,
                selectionArgs);

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return rows;
        } else {
            throw new SQLiteException("Update error:" + uri);
        }
    }


    private class MediaCenterProviderHelper extends SQLiteOpenHelper {

        MediaCenterProviderHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + AUDIO_FOLDERS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AUDIO_TRACKS_TABLE_NAME);

        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "onCreate audio folders database! " + CREATE_AUDIO_FOLDERS_TABLE_SQL);

            db.execSQL(CREATE_AUDIO_FOLDERS_TABLE_SQL);

            Log.d(TAG, "create audio tracks database! " + CREATE_AUDIO_TRACKS_TABLE_SQL);

            db.execSQL(CREATE_AUDIO_TRACKS_TABLE_SQL);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int version1, int version2) {
            dropTables(db);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int version1, int version2) {
            dropTables(db);
            onCreate(db);

        }
    }
}
