package com.fesskiev.mediacenter.ui.audio;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.Group;
import com.fesskiev.mediacenter.data.source.DataRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AudioGroupsViewModel extends ViewModel {

    private final MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>();

    @Inject
    DataRepository repository;
    @SuppressLint("StaticFieldLeak")
    @Inject
    Context context;

    private CompositeDisposable disposables;

    public AudioGroupsViewModel() {
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

    public void getGroups() {
        disposables.add(Observable.zip(repository.getGenresList(), repository.getArtistsList(),
                (genres, artists) -> Group.makeGroups(context, genres, artists))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyGroups));
    }

    private void notifyGroups(List<Group> groups) {
        groupsLiveData.setValue(groups);
    }

    public MutableLiveData<List<Group>> getGroupsLiveData() {
        return groupsLiveData;
    }
}
