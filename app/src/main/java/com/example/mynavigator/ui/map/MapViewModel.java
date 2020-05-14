package com.example.mynavigator.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여러분의 위치를 지도상에 표시합니다\n" +
                "다발지역 위험도는 해당 장소가 얼마나 많은 보행자 다발지역의 반경 내에 걸쳐있는지를 표시합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}