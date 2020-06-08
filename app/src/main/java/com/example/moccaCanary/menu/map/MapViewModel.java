package com.example.moccaCanary.menu.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private float DISTANCETO_PARAMETER = 500;
    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("주변의 다발지역, 사고 정보를 살펴보세요\n"
              );
    }

    public LiveData<String> getText() {
        return mText;
    }
}