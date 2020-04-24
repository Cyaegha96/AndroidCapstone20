package com.example.mynavigator.ui.data;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.mynavigator.R;

public class ItemView extends LinearLayout {

    TextView[] textview = new TextView[14];

    public ItemView(Context context) {
        super(context);

        init(context);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.fragment_data_sector,this,true);

        textview[0] = (TextView) findViewById(R.id.data_sector0);
        textview[1] = (TextView) findViewById(R.id.data_sector1);
        textview[2] = (TextView) findViewById(R.id.data_sector2);
        textview[3] = (TextView) findViewById(R.id.data_sector3);
        textview[4] = (TextView) findViewById(R.id.data_sector4);
        textview[5] = (TextView) findViewById(R.id.data_sector5);
        textview[6] = (TextView) findViewById(R.id.data_sector6);
        textview[7] = (TextView) findViewById(R.id.data_sector7);
        textview[8] = (TextView) findViewById(R.id.data_sector8);
        textview[9] = (TextView) findViewById(R.id.data_sector9);
        textview[10] = (TextView) findViewById(R.id.data_sector10);
        textview[11] = (TextView) findViewById(R.id.data_sector11);
        textview[12] = (TextView) findViewById(R.id.data_sector12);
        textview[13] = (TextView) findViewById(R.id.data_sector13);
        /*
        for(int i=0;i<=13;i++){
            int getID = getResources().getIdentifier("data_sector"+i,"id","R");
            textview[i] = (TextView) findViewById(getID);

         }
    */
    }

    public void setTextview(Data data){
        for(int i=0;i<14;i++){
            String getdata="";
            if(i==0)
                getdata += data.getAccidentCode()+"";
            else if(i==1)
                getdata += data.getAccidentYear()+"";
            else if(i==2)
                getdata += data.getAccidentType();
            else if(i==3)
                getdata += data.getPlaceCode()+"";
            else if(i==4)
                getdata += data.getCityName();
            else if(i==5)
                getdata += data.getPlaceName();
            else if(i==6)
                getdata += data.getAccidentCount()+"";
            else if(i==7)
                getdata += data.getCasualtiesCount()+"";
            else if(i==8)
                getdata += data.getDeadCount()+"";
            else if(i==9)
                getdata += data.getSeriousCount()+"";
            else if(i==10)
                getdata+= data.getSeriousCount()+"";
            else if(i==11)
                getdata+= data.getInjuredCount()+"";
            else if(i==12)
                getdata+= data.getLatitude()+"";
            else if(i==13)
                getdata+= data.getLongitude()+"";
            else
                getdata += "뭐임";

            if(getdata == null)
                getdata += "데이터없음";
            textview[i].setText(getdata);
        }

    }

}
