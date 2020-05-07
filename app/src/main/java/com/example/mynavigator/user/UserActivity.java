package com.example.mynavigator.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;

import java.util.Calendar;

public class UserActivity extends AppCompatActivity {

    ArrayAdapter<CharSequence>  adspin1, adspin2;
    private EditText editUserName;
    private EditText editUserYear;
    private Button saveButton,cancelButton;

    private String user_name="";
    private String choice_do="";
    private String choice_city="";
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

        final Spinner spin_province = findViewById(R.id.spinner_province);
        final Spinner spin_city = findViewById(R.id.spinner_city);

        editUserName = findViewById(R.id.editUserName);
        editUserYear = findViewById(R.id.editYear);
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

                SharedPreferences userInfo = getSharedPreferences("userInfo",MODE_PRIVATE);
                SharedPreferences.Editor editor  = userInfo.edit();
                editor.putString("name",user_name);
                editor.putInt("year",userYear);
                editor.putString("choice_do",choice_do);
                editor.putString("choice_city",choice_city);
                editor.commit();

                Intent intent = new Intent(UserActivity.this, MainActivity.class);
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

        spin_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adspin1.getItem(i).equals("서울특별시")) {
                    choice_do = "서울";//버튼 클릭시 출력을 위해 값을 넣었습니다.
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_seoul, android.R.layout.simple_spinner_dropdown_item);
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

                } else if (adspin1.getItem(i).equals("부산광역시")) {
                    choice_do = "부산";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_busan, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("대구광역시")) {
                    choice_do = "대구";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_daegu, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("인천광역시")) {
                    choice_do = "인천";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_incheon, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("광주광역시")) {
                    choice_do = "광주";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_gwangju, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("대전광역시")) {
                    choice_do = "대전";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_daejeon, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("울산광역시")) {
                    choice_do = "울산";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_ulsan, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("세종특별자치시")) {
                    choice_do = "세종";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_sejong, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("경기도")) {
                    choice_do = "경기도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_gyeonggi, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("강원도")) {
                    choice_do = "강원도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_gangwon, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("충청북도")) {
                    choice_do = "충청북도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_chung_buk, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("충청남도")) {
                    choice_do = "충청남도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_chung_nam, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("전라북도")) {
                    choice_do = "전라북도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_jeon_buk, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("전라남도")) {
                    choice_do = "전라남도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_jeon_nam, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("경상북도")) {
                    choice_do = "경상북도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_gyeong_buk, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("경상남도")) {
                    choice_do = "경상남도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_gyeong_nam, android.R.layout.simple_spinner_dropdown_item);
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
                } else if (adspin1.getItem(i).equals("제주특별자치도")) {
                    choice_do = "제주특별자치도";
                    adspin2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_region_jeju, android.R.layout.simple_spinner_dropdown_item);
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

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
