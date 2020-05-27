package com.example.moccaCanary.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class UserActivity extends AppCompatActivity {
    public static Context mcontext;
    ArrayAdapter<CharSequence>  adspin1, adspin2;
    private EditText editUserName;
    private EditText editUserYear;
    private Button saveButton,cancelButton;

    String province[] = {"서울", "부산", "대구","인천","광주","대전","울산","세종특별자치시","경기도","강원도","충청북도","충청남도","전라북도","전라남도","경상북도", "경상남도", "제주특별자치도"};
    ArrayList<String> provinces = new ArrayList<>(Arrays.asList(province));
    public String user_name="";
    public String choice_do="";
    public String choice_city="";

    private int userYear;
    private static final int MAX_YEAR = 2099;
    private static final int MIN_YEAR = 1920;

    private DatePickerDialog.OnDateSetListener onDateSetListener;
    public Calendar cal = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            userYear = year;
            editUserYear.setText(year+"년 출생");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getSupportActionBar().setElevation(0);
        mcontext = this;
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userName = userInfo.getString("name", "이름 없는 사용자");
        userInfo.getString("choice_do", "지역 정보 없음");
        userInfo.getString("choice_city", "도시 정보 없음");
        int year = userInfo.getInt("year", -1);

        final Spinner spin_province = findViewById(R.id.spinner_province);
        final Spinner spin_city = findViewById(R.id.spinner_city);

        editUserName = findViewById(R.id.editUserName);
        if (!userName.equals("이름 없는 사용자")) {
            editUserName.setHint(userName);
        }

        editUserYear = findViewById(R.id.editYear);
        if (year != -1) {
            editUserYear.setText(year + "년 출생");
            userYear = year;
        }

        editUserYear.setFocusable(false);
        editUserYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyYearMonthPickerDialog pd = new MyYearMonthPickerDialog();
                pd.setOnDateSetListener(d);
                pd.show(getSupportFragmentManager(), "YearMonthPickerTest");
            }
        });

        saveButton = findViewById(R.id.SaveUserInfoButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name = editUserName.getText().toString();

                SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putString("name", user_name);
                editor.putInt("year", userYear);
                editor.putString("choice_do", choice_do);
                editor.putString("choice_city", choice_city);
                editor.commit();

                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();

            }
        });
        cancelButton = findViewById(R.id.CancelUserInfoButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        adspin1 = ArrayAdapter.createFromResource(this, R.array.spinner_region, android.R.layout.simple_spinner_dropdown_item);
        adspin1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_province.setAdapter(adspin1);

        if(provinces.contains(choice_do))
            spin_province.setSelection(provinces.indexOf(choice_do));


        spin_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                //첫번째 spinner 클릭시 이벤트 발생입니다. setO 정도까지 치시면 자동완성됩니다. 뒤에도 마찬가지입니다.
                int city[] = {R.array.spinner_region_seoul, R.array.spinner_region_busan,  R.array.spinner_region_daegu, R.array.spinner_region_incheon, R.array.spinner_region_gwangju, R.array.spinner_region_daejeon, R.array.spinner_region_ulsan, R.array.spinner_region_sejong, R.array.spinner_region_gyeonggi, R.array.spinner_region_gangwon, R.array.spinner_region_chung_buk, R.array.spinner_region_chung_nam, R.array.spinner_region_jeon_buk, R.array.spinner_region_jeon_nam, R.array.spinner_region_gyeong_buk, R.array.spinner_region_gyeong_nam, R.array.spinner_region_jeju};
                for (int j = 0; j < adspin1.getCount(); j++) {
                    choice_factor(spin_city, i, spin_province.getItemAtPosition(j).toString(), province[j], city[j]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    void choice_factor(Spinner spin_city, int i, String province, String choice, int Spinner){
        if (adspin1.getItem(i).equals(province)) {
            choice_do = choice;//버튼 클릭시 출력을 위해 값을 넣었습니다.
            adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), Spinner, android.R.layout.simple_spinner_dropdown_item);
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
