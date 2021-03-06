package com.mocca.moccaCanary.menu.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("주변의 다발지역, 사고 정보를 살펴보세요\n마커를 클릭해서 어떤 사건이 있었는지 알아보세요"
              );
    }

    public LiveData<String> getText() {
        return mText;
    }
}