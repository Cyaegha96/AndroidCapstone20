package com.example.moccaCanary.menu.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DataViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DataViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("도로교통공단 교통사고다발지역 20191010\n공공데이터포탈 제공");
    }

    public LiveData<String> getText() {
        return mText;
    }
}