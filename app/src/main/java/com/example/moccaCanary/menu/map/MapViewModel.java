package com.example.moccaCanary.menu.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private float DISTANCETO_PARAMETER = 500;
    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("주변의 다발지역, 사고 정보를 살펴보세요\n" +
                "눈 아이콘은 지도의 중앙을 가리키며, 중앙으로부터 반경 " + DISTANCETO_PARAMETER+"m 만큼의 데이터가 보여집니다.\n"+"@"
        +"*지원지역이 아닌 경우 사망자 데이터만 표기됩니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}