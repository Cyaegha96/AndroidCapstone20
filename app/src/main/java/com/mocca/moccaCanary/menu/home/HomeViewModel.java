package com.mocca.moccaCanary.menu.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("아래 버튼을 눌러 위치 서비스를 활성화 시키세요");
    }

    public LiveData<String> getText() {
        return mText;
    }
}