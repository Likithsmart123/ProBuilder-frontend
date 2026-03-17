package com.example.probuilder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class ProjectDetailViewModel extends ViewModel {

    public MutableLiveData<List<PhotoItem>> photos = new MutableLiveData<>();
    public MutableLiveData<Boolean> photosLoaded = new MutableLiveData<>(false);

    public void setPhotos(List<PhotoItem> list) {
        photos.setValue(list);
        photosLoaded.setValue(true);
    }
}
