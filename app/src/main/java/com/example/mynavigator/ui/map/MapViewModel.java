package com.example.mynavigator.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여러분의 위치를 지도상에 표시합니다");
    }

    public LiveData<String> getText() {
        return mText;
    }
}