package com.example.moccaCanary.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //slide
        addSlide(AppIntroFragment.newInstance("안녕하세요", "보행자용 위험지역 알리미 카나리아 입니다"
                , R.raw.c1, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("앱을 활성화 해주세요", "활성시 위치정보를 받아옵니다"
                , R.raw.c2, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("알림 확인", "활성시 알림창에서 정보 확인이 가능합니다"
                , R.raw.c3, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("메뉴화면", "메뉴에서는 여러가지 정보를 확인 가능합니다"
                , R.raw.menu, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("지도보기", "지도에서 위험 지역, 사고유형등 확인이 가능합니다"
                , R.raw.c4, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("그래프 보기", "보기 쉽게 그래프로 확인도 가능해요"
                , R.raw.gr, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("웹뷰보기", "교통사고분석사이트를 바로 열람 할 수 있습니다"
                , R.raw.c7, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("제보하기", "위험지역을 제보해서 위험 정보를 알려주세요! 위치를 꾹 눌러 위치 지정 후"
                , R.raw.c81, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("제보하기", "유형을 정해서 보내주세요. 함께 만들어 갑시다!"
                , R.raw.c82, ContextCompat.getColor(getApplication(),
                        R.color.color1)));
        addSlide(AppIntroFragment.newInstance("카나리아", "시작 해보세요!"
                , R.raw.c9, ContextCompat.getColor(getApplication(),
                        R.color.color1)));

    }
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences tipfirst = getSharedPreferences("checkFirst",MODE_PRIVATE);
        boolean checkFirst = tipfirst.getBoolean("checkFirst",false);
        if(!checkFirst) {
            SharedPreferences.Editor editor = tipfirst.edit();
            editor.putBoolean("checkFirst", true);
            editor.commit();
            startUserActivity();
            finish();
        }
        else
            startMainActivity();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences tipfirst = getSharedPreferences("checkFirst",MODE_PRIVATE);
        boolean checkFirst = tipfirst.getBoolean("checkFirst",false);
        if(!checkFirst) {
            SharedPreferences.Editor editor = tipfirst.edit();
            editor.putBoolean("checkFirst", true);
            editor.commit();
            startUserActivity();
            finish();
        }
        else
            startMainActivity();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment,
                               @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startUserActivity() {
        Intent intent = new Intent(IntroActivity.this, UserActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
