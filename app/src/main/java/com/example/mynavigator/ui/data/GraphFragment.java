package com.example.mynavigator.ui.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mynavigator.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GraphFragment extends Fragment {

    ArrayAdapter<CharSequence> accident, adspin1, adspin2; //어댑터를 선언했습니다. adspint1(서울,인천..) adspin2(강남구,강서구..)
    String province[] = {"서울", "부산", "대구","인천","광주","대전","울산","세종특별자치시","경기도","강원도","충청북도","충청남도","전라북도","전라남도","경상북도", "경상남도", "제주특별자치도"};
    ArrayList<String> provinces = new ArrayList<>(Arrays.asList(province));

    private String choice_do = "";
    private String choice_city = "";
    private String choice_accident = "";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_graph_view, container, false);
        return root;
    }

    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);


        SharedPreferences userInfo = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE );

        choice_do = userInfo.getString("choice_do","지역 정보 없음");
        choice_city = userInfo.getString("choice_city","도시 정보 없음");



        DataBaseHelper mdb = new DataBaseHelper(getContext(), "data_all.db", 1);
        final SQLiteDatabase sqdb = mdb.getWritableDatabase();
        final LineChart lineChart = getActivity().findViewById(R.id.chart);


        final Spinner accident_type = getActivity().findViewById(R.id.accident_type);
        final Spinner spin_province = getActivity().findViewById(R.id.spinner_province);
        final Spinner spin_city = getActivity().findViewById(R.id.spinner_city);

        accident_type.setPrompt("사고 유형 선택");
        spin_province.setPrompt("행정 지역 선택");
        spin_city.setPrompt("시군구 선택");

        Button btn_refresh = getActivity().findViewById(R.id.btn_refresh);

        accident = ArrayAdapter.createFromResource(getContext(), R.array.accident_type, android.R.layout.simple_spinner_dropdown_item);
        accident.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accident_type.setAdapter(accident);



        accident_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choice_accident = accident_type.getSelectedItem().toString();
                if(choice_accident.equals("사고발생건수"))
                    choice_accident = "accidentCount";
                else if(choice_accident.equals("사상자수"))
                    choice_accident = "casualtiesCount";
                else if(choice_accident.equals("사망자수"))
                    choice_accident = "deadCount";
                else if(choice_accident.equals("중상자수"))
                    choice_accident = "seriousCount";
                else if(choice_accident.equals("경상자수"))
                    choice_accident = "slightlyCount";
                else if(choice_accident.equals("부상자수"))
                    choice_accident = "injuredCount";

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        adspin1 = ArrayAdapter.createFromResource(getContext(), R.array.spinner_region, android.R.layout.simple_spinner_dropdown_item);
        adspin1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_province.setAdapter(adspin1);

        if(provinces.contains(choice_do))
            spin_province.setSelection(provinces.indexOf(choice_do));

        spin_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //첫번째 spinner 클릭시 이벤트 발생입니다. setO 정도까지 치시면 자동완성됩니다. 뒤에도 마찬가지입니다.
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int city[] = {R.array.spinner_region_seoul, R.array.spinner_region_busan,  R.array.spinner_region_daegu, R.array.spinner_region_incheon, R.array.spinner_region_gwangju, R.array.spinner_region_daejeon, R.array.spinner_region_ulsan, R.array.spinner_region_sejong, R.array.spinner_region_gyeonggi, R.array.spinner_region_gangwon, R.array.spinner_region_chung_buk, R.array.spinner_region_chung_nam, R.array.spinner_region_jeon_buk, R.array.spinner_region_jeon_nam, R.array.spinner_region_gyeong_buk, R.array.spinner_region_gyeong_nam, R.array.spinner_region_jeju};
                for (int j = 0; j < adspin1.getCount(); j++) {
                    choice_factor(spin_city, i, spin_province.getItemAtPosition(j).toString(), province[j], city[j]);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {

            //버튼 클릭시 이벤트입니다.
            @Override
            public void onClick(View view) {
                Cursor c = sqdb.rawQuery("select accidentYear, sum(" + choice_accident + ") from sample_table where cityName LIKE \""+choice_do+"%"+choice_city+"%\"  group by accidentYear", null);

                List<Entry> entries = new ArrayList<>();

                while (c.moveToNext()) {
                    entries.add(new Entry(c.getInt(0), c.getInt(1)));
                }

                LineDataSet lineDataSet = new LineDataSet(entries, choice_accident);
                lineDataSet.setLineWidth(2);
                lineDataSet.setCircleRadius(6);
                lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
                lineDataSet.setCircleHoleColor(Color.BLUE);
                lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
                lineDataSet.setDrawCircleHole(true);
                lineDataSet.setDrawCircles(true);
                lineDataSet.setDrawHorizontalHighlightIndicator(false);
                lineDataSet.setDrawHighlightIndicators(false);
                lineDataSet.setDrawValues(false);

                LineData lineData = new LineData(lineDataSet);
                lineChart.setData(lineData);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextColor(Color.BLACK);
                xAxis.enableGridDashedLine(8, 24, 0);

                YAxis yLAxis = lineChart.getAxisLeft();
                yLAxis.setTextColor(Color.BLACK);

                YAxis yRAxis = lineChart.getAxisRight();
                yRAxis.setDrawLabels(false);
                yRAxis.setDrawAxisLine(false);
                yRAxis.setDrawGridLines(false);

                Description description = new Description();
                description.setText("");

                lineChart.setDoubleTapToZoomEnabled(false);
                lineChart.setDrawGridBackground(false);
                lineChart.setDescription(description);
                lineChart.animateY(2000, Easing.EaseInCubic);
                lineChart.invalidate();
            }


        });


    }


    void choice_factor(Spinner spin_city, int i, String province, String choice, int Spinner){
        if (adspin1.getItem(i).equals(province)) {
            choice_do = choice;//버튼 클릭시 출력을 위해 값을 넣었습니다.
            adspin2 = ArrayAdapter.createFromResource(getContext(), Spinner, android.R.layout.simple_spinner_dropdown_item);
            adspin2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_city.setAdapter(adspin2);
            spin_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    choice_city = adspin2.getItem(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        }

    }
}