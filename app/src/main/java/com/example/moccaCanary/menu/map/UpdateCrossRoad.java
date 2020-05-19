package com.example.moccaCanary.menu.map;

import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moccaCanary.R;
import com.example.moccaCanary.menu.data.CwData;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class UpdateCrossRoad {

    private Context context;
    private static final String TAG = "UpdateCrossroad";

    private Bitmap crossRoad1;
    private Bitmap crossRoad2;
    private Bitmap crossRoad3;
    private Bitmap crossRoad4;
    private Bitmap crossRoadnull;

    public UpdateCrossRoad(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final Marker marker,
                             final ImageView bottomSheetCrossroadImageView,
                             final TextView bottomSheetcrossroadType,
                                final TextView bottomSheetPlacename,
                             final List<CwData> cwdataList) {

        crossRoad1 = makeBitmap(R.drawable.crossroad1,150,150);
        crossRoad2 = makeBitmap(R.drawable.crossroad2,150,150);
        crossRoad3 = makeBitmap(R.drawable.crossroad3,150,200);
        crossRoad4 = makeBitmap(R.drawable.crossroad4,150,200);

        crossRoadnull = makeBitmap(R.drawable.crossroadnull,150,150);

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.update_crossroad_dialog);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final EditText message = (EditText) dlg.findViewById(R.id.mesgase);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);
        String RoadNM = marker.getSnippet().split("@")[2];
        message.setText(RoadNM);

        final RadioGroup radioGroup = (RadioGroup) dlg.findViewById(R.id.rg_btGroup);
        final RadioButton rg_btn1 = dlg.findViewById(R.id.rg_btn1);
        final RadioButton rg_btn2 = dlg.findViewById(R.id.rg_btn2);
        final RadioButton rg_btn3 = dlg.findViewById(R.id.rg_btn3);
        final RadioButton rg_btn4 = dlg.findViewById(R.id.rg_btn4);
        final RadioButton rg_btn5 = dlg.findViewById(R.id.rg_btn5);

        String crossRoadType = marker.getSnippet().split("@")[0];
        if(crossRoadType.equals("일반형")){
            rg_btn1.setChecked(true);
        }else if(crossRoadType.equals("대각선")){
            rg_btn2.setChecked(true);
        }else if(crossRoadType.equals("스테거드")){
            rg_btn3.setChecked(true);
        }else if(crossRoadType.equals("도류화")){
            rg_btn4.setChecked(true);
        }else if(crossRoadType.equals("기타")){
            rg_btn5.setChecked(true);
        }else{
            rg_btn5.setChecked(true);
        }



        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) dlg.findViewById(id);

                float latitude = (float) marker.getPosition().latitude;
                float longitude = (float) marker.getPosition().longitude;

                String crossRoadType = rb.getText().toString();
                int crslkKnd = 0;
                String roadNM = message.getText().toString();
                if(roadNM.equals("")){
                    roadNM = "위치정보 없음";
                }

                if(crossRoadType.equals("일반형")){
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad1);
                    crslkKnd = 1;
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoad1));
                }else if(crossRoadType.equals("대각선")){
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad2);
                    crslkKnd = 2;
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoad2));
                }else if(crossRoadType.equals("스테거드")){
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad3);
                    crslkKnd = 3;
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoad3));
                }else if(crossRoadType.equals("도류화")){
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad4);
                    crslkKnd = 4;
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoad4));
                }else if(crossRoadType.equals("기타")){
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroadnull);
                    crslkKnd  = 99;
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoadnull));
                }else{
                    bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroadnull);
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(crossRoadnull));
                }
                bottomSheetcrossroadType.setText(crossRoadType);
                bottomSheetPlacename.setText(message.getText());


                //마커의 내용도 수정하고!
                String snippet = marker.getSnippet();
                Log.d(TAG, "현재 마커 스니퍼 메시지:"+ snippet);
                snippet =  snippet.replace(snippet.split("@")[0] , crossRoadType);
                snippet = snippet.replace(snippet.split("@")[2], message.getText().toString());
                marker.setSnippet(snippet);

                //테이블의 내용도 수정하고!
                for(int i=0;i< cwdataList.size();i++){
                    //만약 마커의 내용과 cwdataList의 내용이 같다면
                    if((marker.getPosition().latitude == cwdataList.get(i).getLatitude())
                    && (marker.getPosition().longitude) == cwdataList.get(i).getLongitude()){
                        //수정하셈 ㅇㅇ
                        cwdataList.get(i).setAccidentType(crossRoadType);
                        cwdataList.get(i).setRoadNm(roadNM);
                    }
                }

                //db의 내용도 수정을 하자고!

                SQLiteDatabase db;
                DataBaseHelper dataBaseHelper =  new DataBaseHelper(context,"data_all.db", 1);
                db = dataBaseHelper.getWritableDatabase();
                Log.d(TAG,"UPDATE cw_table SET crslkKnd = " + crslkKnd+ ", roadNm = '"+ roadNM +
                        "' WHERE index = " + marker.getSnippet().split("@")[4]);

                String sql = "UPDATE cw_table SET crslkKnd = " + crslkKnd+ ", roadNm = '"+ roadNM +
                        "' WHERE cwIndex = " + marker.getSnippet().split("@")[4]+ ";";
                db.execSQL(sql);
                db.close();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }
    private Bitmap makeBitmap(int drawable,int width, int height){
        BitmapDrawable bitmapdraw = (BitmapDrawable) context.getResources().getDrawable(drawable);
        Bitmap b = bitmapdraw.getBitmap();
        return  Bitmap.createScaledBitmap(b, width, height, false);
    }
}