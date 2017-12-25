package com.fesskiev.mediacenter.ui.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.search.Album;
import com.fesskiev.mediacenter.data.model.search.Image;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.remote.retrofit.RetrofitErrorHelper;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class AlbumSearchViewModel extends ViewModel {

    private final MutableLiveData<Bitmap> albumCoverLiveData = new MutableLiveData<>();
    private final MutableLiveData<Album> albumLiveData = new MutableLiveData<>();

    private final SingleLiveEvent<Void> enterArtistErrorLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> enterAlbumErrorLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> responseErrorLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> responseAlbumNotFoundLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> successSaveBitmapLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> errorSaveBitmapLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    BitmapHelper bitmapHelper;
    @SuppressLint("StaticFieldLeak")
    @Inject
    Context context;

    private CompositeDisposable disposables;


    public AlbumSearchViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public void loadAlbum(String artist, String album) {
        if (artist == null || artist.isEmpty()) {
            notifyArtistError();
            return;
        }
        if (album == null || album.isEmpty()) {
            notifyAlbumError();
            return;
        }
        disposables.add(repository.getAlbum(artist.trim(), album.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(albumResponse -> parseAlbum(albumResponse.getAlbum()), this::notifyResponseError));
    }

    private Observable<Bitmap> parseAlbumCover(Album album) {
        if (album != null) {
            List<Image> images = album.getImage();
            if (images != null) {
                for (Image image : images) {
                    if (image.getSize().equals("large")) {
                        String text = image.getText();
                        if (text != null && text.length() != 0) {
                            return bitmapHelper.getCoverBitmapFromURL(text);
                        }
                    }
                }
            }
        }
        return Observable.empty();
    }

    private void parseAlbum(Album album) {
        if (album == null) {
            notifyAlbumNotFound();
            return;
        }
        disposables.add(parseAlbumCover(album)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyAlbumCover, Throwable::printStackTrace));

        notifyAlbumFound(album);
    }

    public void loadSelectedImage(Image image, AudioFolder audioFolder) {
        disposables.add(bitmapHelper.getCoverBitmapFromURL(image.getText())
                .subscribeOn(Schedulers.io())
                .doOnNext(bitmap -> removeFolderImages(audioFolder))
                .flatMap(bitmap -> saveArtworkAndUpdateFolder(bitmap, audioFolder))
                .flatMap(object -> repository.getAudioTracks(audioFolder.id))
                .flatMap(Observable::fromIterable)
                .flatMap(audioFile -> {
                    audioFile.folderArtworkPath = audioFolder.folderImage.getAbsolutePath();
                    return repository.updateAudioFile(audioFile);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> notifySuccessSaveBitmap(),
                        throwable -> notifyErrorSaveBitmap()));
    }

    private Observable<Object> saveArtworkAndUpdateFolder(Bitmap bitmap, AudioFolder audioFolder) {
        try {
            File path = File.createTempFile(audioFolder.folderName, ".jpg", audioFolder.folderPath);

            BitmapHelper.saveBitmap(bitmap, path);

            audioFolder.folderImage = path;

        } catch (IOException e) {
            e.printStackTrace();
            notifyErrorSaveBitmap();
        }
        return repository.updateAudioFolder(audioFolder);
    }

    private void removeFolderImages(AudioFolder audioFolder) {
        File[] filterImages = audioFolder.folderPath.listFiles(FileSystemService.folderImageFilter());
        if (filterImages != null && filterImages.length > 0) {
            for (File image : filterImages) {
                image.delete();
            }
        }
    }

    private void notifyResponseError(Throwable throwable) {
        throwable.printStackTrace();
        String message = RetrofitErrorHelper.getErrorDescription(context, throwable);
        responseErrorLiveData.setValue(message);
    }

    private void notifyArtistError() {
        enterArtistErrorLiveData.call();
    }

    private void notifyAlbumError() {
        enterAlbumErrorLiveData.call();
    }

    private void notifyAlbumCover(Bitmap bitmap) {
        albumCoverLiveData.setValue(bitmap);
    }

    private void notifyAlbumNotFound() {
        responseAlbumNotFoundLiveData.call();
    }

    private void notifyErrorSaveBitmap() {
        errorSaveBitmapLiveData.call();
    }

    private void notifySuccessSaveBitmap() {
        successSaveBitmapLiveData.call();
    }

    private void notifyAlbumFound(Album album) {
        albumLiveData.setValue(album);
    }

    public SingleLiveEvent<Void> getEnterArtistErrorLiveData() {
        return enterArtistErrorLiveData;
    }

    public SingleLiveEvent<Void> getEnterAlbumErrorLiveData() {
        return enterAlbumErrorLiveData;
    }

    public SingleLiveEvent<String> getResponseErrorLiveData() {
        return responseErrorLiveData;
    }

    public SingleLiveEvent<Void> getResponseAlbumNotFoundLiveData() {
        return responseAlbumNotFoundLiveData;
    }

    public MutableLiveData<Bitmap> getAlbumCoverLiveData() {
        return albumCoverLiveData;
    }

    public MutableLiveData<Album> getAlbumLiveData() {
        return albumLiveData;
    }

    public SingleLiveEvent<Void> getSuccessSaveBitmapLiveData() {
        return successSaveBitmapLiveData;
    }

    public SingleLiveEvent<Void> getErrorSaveBitmapLiveData() {
        return errorSaveBitmapLiveData;
    }
}
