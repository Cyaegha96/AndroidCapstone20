package com.mocca.moccaCanary.menu.data;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mocca.moccaCanary.R;



public class WebViewFragment extends Fragment {



    private String myUrl = "http://taas.koroad.or.kr/";// 접속 URL (내장HTML의 경우 왼쪽과 같이 쓰고 아니면 걍 URL)
    private String mCurrentUrl;
    public WebView mWebView;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:{
                    webViewGoBack();
                }break;
            }
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_web_view, container, false);
        mWebView = (WebView) v.findViewById(R.id.web_view);
        mWebView.setHorizontalScrollBarEnabled(false); // 가로 스크롤 방지
        mWebView.setVerticalScrollBarEnabled(false); // 세로 스크롤 방지
        mWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // 속도 향상
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 속도 향상
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 캐시모드
        mWebView.setWebViewClient(new WebViewClient()); // 이걸 안해주면 새창이 뜸
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // width, height가 화면 크기와 맞지 않는 버그 해결
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // javascript의 window.opne 허용
        mWebView.getSettings().setJavaScriptEnabled(true); // 자바 스크립트 허용
        mWebView.getSettings().setUseWideViewPort(true); //meta태그의 viewport사용 가능
        mWebView.loadUrl(myUrl);

        mWebView.setOnKeyListener(new View.OnKeyListener(){

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == MotionEvent.ACTION_UP
                        && mWebView.canGoBack()) {
                    handler.sendEmptyMessage(1);
                    return true;
                }

                return false;
            }

        });

        return v;
    }
    private void webViewGoBack(){
        mWebView.goBack();
    }
}
