package com.hangisool.aircleaner_2.setting;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hangisool.aircleaner_2.MainActivity;
import com.hangisool.aircleaner_2.R;
import com.hangisool.aircleaner_2.Util.Address;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hangisool.aircleaner_2.MainActivity.PROGRESSDIALOG;

/**
 * Created by L G on 2018-03-15.
 */

public class ReservationActivity extends AppCompatActivity{
    Button btn_Reserve1_Start, btn_Reserve2_Start, btn_Reserve3_Start, btn_Reserve4_Start ,btn_Reserve5_Start;
    Button btn_Reserve1_End, btn_Reserve2_End, btn_Reserve3_End, btn_Reserve4_End ,btn_Reserve5_End;
    Button btn_Reserve_Save;
    Spinner spin_Reserve1_week, spin_Reserve2_week, spin_Reserve3_week, spin_Reserve4_week, spin_Reserve5_week;
    TextView txt_Reserve1_week, txt_Reserve2_week, txt_Reserve3_week, txt_Reserve4_week, txt_Reserve5_week;
    CheckBox check_Reserve1, check_Reserve2, check_Reserve3, check_Reserve4, check_Reserve5;
    boolean checkFirstSelectSpin1 = true;//처음 요일 스피너가 생성될때 Select메소드를 속에서 txt_reserve 내용을 안바꾸게 하기위해 처음 1번은 true라 안들어가도록하기위해
    boolean checkFirstSelectSpin2 = true;
    boolean checkFirstSelectSpin3 = true;
    boolean checkFirstSelectSpin4 = true;
    boolean checkFirstSelectSpin5 = true;
    String reserve_data;
    String device_id;//이전 프라그먼트에서 사용자가 선택한 디바이스 아이디
    ProgressDialog progressDialog;
    //프로그래스다이얼로그 UI그리는 작업을 위한 핸들러
    Handler mHandler = null;
    //예약정보 데이터
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        mHandler = new Handler();

        //UI 객체생성
        btn_Reserve1_Start = (Button) findViewById(R.id.btn_reserve1_start);
        btn_Reserve1_End = (Button) findViewById(R.id.btn_reserve1_end);
        btn_Reserve2_Start = (Button) findViewById(R.id.btn_reserve2_start);
        btn_Reserve2_End = (Button) findViewById(R.id.btn_reserve2_end);
        btn_Reserve3_Start = (Button) findViewById(R.id.btn_reserve3_start);
        btn_Reserve3_End = (Button) findViewById(R.id.btn_reserve3_end);
        btn_Reserve4_Start = (Button) findViewById(R.id.btn_reserve4_start);
        btn_Reserve4_End = (Button) findViewById(R.id.btn_reserve4_end);
        btn_Reserve5_Start = (Button) findViewById(R.id.btn_reserve5_start);
        btn_Reserve5_End = (Button) findViewById(R.id.btn_reserve5_end);
        btn_Reserve_Save = (Button) findViewById(R.id.btn_reserve_save);
        spin_Reserve1_week = (Spinner) findViewById(R.id.spin_week_reserve1);
        spin_Reserve2_week = (Spinner) findViewById(R.id.spin_week_reserve2);
        spin_Reserve3_week = (Spinner) findViewById(R.id.spin_week_reserve3);
        spin_Reserve4_week = (Spinner) findViewById(R.id.spin_week_reserve4);
        spin_Reserve5_week = (Spinner) findViewById(R.id.spin_week_reserve5);
        txt_Reserve1_week = (TextView) findViewById(R.id.txt_week_reserve1);
        txt_Reserve2_week = (TextView) findViewById(R.id.txt_week_reserve2);
        txt_Reserve3_week = (TextView) findViewById(R.id.txt_week_reserve3);
        txt_Reserve4_week = (TextView) findViewById(R.id.txt_week_reserve4);
        txt_Reserve5_week = (TextView) findViewById(R.id.txt_week_reserve5);
        check_Reserve1 = (CheckBox) findViewById(R.id.checkbox_reserve1);
        check_Reserve2 = (CheckBox) findViewById(R.id.checkbox_reserve2);
        check_Reserve3 = (CheckBox) findViewById(R.id.checkbox_reserve3);
        check_Reserve4 = (CheckBox) findViewById(R.id.checkbox_reserve4);
        check_Reserve5 = (CheckBox) findViewById(R.id.checkbox_reserve5);

        //데이터 가져오기
        Intent intent = getIntent();
        reserve_data = intent.getStringExtra("data");
        device_id = intent.getStringExtra("device_id");
        //data = "09:00~16:30-7-1/09:00~10:00-7-1///";
        if (reserve_data != null) {
            String[] reserveData;
            reserveData = reserve_data.split("/");
            for (int i = 0; i < reserveData.length; i++) {
                if (!(reserveData[i].equals(""))) {//예약정보에 무슨 값이 들어있을때
                    switch (i) {
                        case 0:
                            btn_Reserve1_Start.setText(reserveData[i].substring(0, 5));//시작시간
                            btn_Reserve1_End.setText(reserveData[i].substring(6, 11));//끝나는시간
                            switch (reserveData[i].substring(12, 13)) {//요일
                                case "0"://일
                                    txt_Reserve1_week.setText("Sun");
                                    break;
                                case "1"://월
                                    txt_Reserve1_week.setText("Mon");
                                    break;
                                case "2"://화
                                    txt_Reserve1_week.setText("Tue");
                                    break;
                                case "3"://수
                                    txt_Reserve1_week.setText("Wed");
                                    break;
                                case "4"://목
                                    txt_Reserve1_week.setText("Thu");
                                    break;
                                case "5"://금
                                    txt_Reserve1_week.setText("Fri");
                                    break;
                                case "6"://토
                                    txt_Reserve1_week.setText("Sat");
                                    break;
                                case "7"://매일
                                    txt_Reserve1_week.setText("Eve");
                                    break;
                            }
                            if (reserveData[i].substring(14, 15).equals("1")) {//활성화 여부
                                check_Reserve1.setChecked(true);
                            } else {
                                check_Reserve1.setChecked(false);
                            }
                            break;
                        case 1:
                            btn_Reserve2_Start.setText(reserveData[i].substring(0, 5));//시작시간
                            btn_Reserve2_End.setText(reserveData[i].substring(6, 11));//끝나는시간
                            switch (reserveData[i].substring(12, 13)) {//요일
                                case "0"://일
                                    txt_Reserve2_week.setText("Sun");
                                    break;
                                case "1"://월
                                    txt_Reserve2_week.setText("Mon");
                                    break;
                                case "2"://화
                                    txt_Reserve2_week.setText("Tue");
                                    break;
                                case "3"://수
                                    txt_Reserve2_week.setText("Wed");
                                    break;
                                case "4"://목
                                    txt_Reserve2_week.setText("Thu");
                                    break;
                                case "5"://금
                                    txt_Reserve2_week.setText("Fri");
                                    break;
                                case "6"://토
                                    txt_Reserve2_week.setText("Sat");
                                    break;
                                case "7"://매일
                                    txt_Reserve2_week.setText("Eve");
                                    break;
                            }
                            if (reserveData[i].substring(14, 15).equals("1")) {//활성화 여부
                                check_Reserve2.setChecked(true);
                            } else {
                                check_Reserve2.setChecked(false);
                            }
                            break;
                        case 2:
                            btn_Reserve3_Start.setText(reserveData[i].substring(0, 5));//시작시간
                            btn_Reserve3_End.setText(reserveData[i].substring(6, 11));//끝나는시간
                            switch (reserveData[i].substring(12, 13)) {//요일
                                case "0"://일
                                    txt_Reserve3_week.setText("Sun");
                                    break;
                                case "1"://월
                                    txt_Reserve3_week.setText("Mon");
                                    break;
                                case "2"://화
                                    txt_Reserve3_week.setText("Tue");
                                    break;
                                case "3"://수
                                    txt_Reserve3_week.setText("Wed");
                                    break;
                                case "4"://목
                                    txt_Reserve3_week.setText("Thu");
                                    break;
                                case "5"://금
                                    txt_Reserve3_week.setText("Fri");
                                    break;
                                case "6"://토
                                    txt_Reserve3_week.setText("Sat");
                                    break;
                                case "7"://매일
                                    txt_Reserve3_week.setText("Eve");
                                    break;
                            }
                            if (reserveData[i].substring(14, 15).equals("1")) {//활성화 여부
                                check_Reserve3.setChecked(true);
                            } else {
                                check_Reserve3.setChecked(false);
                            }
                            break;
                        case 3:
                            btn_Reserve4_Start.setText(reserveData[i].substring(0, 5));//시작시간
                            btn_Reserve4_End.setText(reserveData[i].substring(6, 11));//끝나는시간
                            switch (reserveData[i].substring(12, 13)) {//요일
                                case "0"://일
                                    txt_Reserve4_week.setText("Sun");
                                    break;
                                case "1"://월
                                    txt_Reserve4_week.setText("Mon");
                                    break;
                                case "2"://화
                                    txt_Reserve4_week.setText("Tue");
                                    break;
                                case "3"://수
                                    txt_Reserve4_week.setText("Wed");
                                    break;
                                case "4"://목
                                    txt_Reserve4_week.setText("Thu");
                                    break;
                                case "5"://금
                                    txt_Reserve4_week.setText("Fri");
                                    break;
                                case "6"://토
                                    txt_Reserve4_week.setText("Sat");
                                    break;
                                case "7"://매일
                                    txt_Reserve4_week.setText("Eve");
                                    break;
                            }
                            if (reserveData[i].substring(14, 15).equals("1")) {//활성화 여부
                                check_Reserve4.setChecked(true);
                            } else {
                                check_Reserve4.setChecked(false);
                            }
                            break;
                        case 4:
                            btn_Reserve5_Start.setText(reserveData[i].substring(0, 5));//시작시간
                            btn_Reserve5_End.setText(reserveData[i].substring(6, 11));//끝나는시간
                            switch (reserveData[i].substring(12, 13)) {//요일
                                case "0"://일
                                    txt_Reserve5_week.setText("Sun");
                                    break;
                                case "1"://월
                                    txt_Reserve5_week.setText("Mon");
                                    break;
                                case "2"://화
                                    txt_Reserve5_week.setText("Tue");
                                    break;
                                case "3"://수
                                    txt_Reserve5_week.setText("Wed");
                                    break;
                                case "4"://목
                                    txt_Reserve5_week.setText("Thu");
                                    break;
                                case "5"://금
                                    txt_Reserve5_week.setText("Fri");
                                    break;
                                case "6"://토
                                    txt_Reserve5_week.setText("Sat");
                                    break;
                                case "7"://매일
                                    txt_Reserve5_week.setText("Eve");
                                    break;
                            }
                            if (reserveData[i].substring(14, 15).equals("1")) {//활성화 여부
                                check_Reserve5.setChecked(true);
                            } else {
                                check_Reserve5.setChecked(false);
                            }
                            break;
                    }
                }
            }
        }

        btn_Reserve1_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve1_Start);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });
        btn_Reserve1_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve1_End);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });

        btn_Reserve2_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve2_Start);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });
        btn_Reserve2_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve2_End);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });

        btn_Reserve3_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve3_Start);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });
        btn_Reserve3_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve3_End);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });

        btn_Reserve4_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve4_Start);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });
        btn_Reserve4_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve4_End);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });

        btn_Reserve5_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve1_Start);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });
        btn_Reserve5_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment mTimePickerFragment = new TimePickerFragment(btn_Reserve5_End);
                mTimePickerFragment.show(getSupportFragmentManager(), "HI");
            }
        });

        spin_Reserve1_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!checkFirstSelectSpin1) {
                    String weekFullName = "";
                    String weekShotName = "";
                    weekFullName = spin_Reserve1_week.getItemAtPosition(position).toString();//스피너에서 고른것을 스피너위의 테스트뷰에 표시해주기 위해
                    weekShotName = weekFullName.substring(0, 3);
                    txt_Reserve1_week.setText(weekShotName);
                }
                checkFirstSelectSpin1 = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spin_Reserve2_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!checkFirstSelectSpin2) {
                    String weekFullName = "";
                    String weekShotName = "";
                    weekFullName = spin_Reserve2_week.getItemAtPosition(position).toString();//스피너에서 고른것을 스피너위의 테스트뷰에 표시해주기 위해
                    weekShotName = weekFullName.substring(0, 3);
                    txt_Reserve2_week.setText(weekShotName);
                }
                checkFirstSelectSpin2 = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txt_Reserve2_week.setText("None");
            }
        });
        spin_Reserve3_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!checkFirstSelectSpin3) {
                    String weekFullName = "";
                    String weekShotName = "";
                    weekFullName = spin_Reserve3_week.getItemAtPosition(position).toString();//스피너에서 고른것을 스피너위의 테스트뷰에 표시해주기 위해
                    weekShotName = weekFullName.substring(0, 3);
                    txt_Reserve3_week.setText(weekShotName);
                }
                checkFirstSelectSpin3 = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spin_Reserve4_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!checkFirstSelectSpin4){
                    String weekFullName = "";
                    String weekShotName = "";
                    weekFullName = spin_Reserve4_week.getItemAtPosition(position).toString();//스피너에서 고른것을 스피너위의 테스트뷰에 표시해주기 위해
                    weekShotName = weekFullName.substring(0,3);
                    txt_Reserve4_week.setText(weekShotName);
                }
                checkFirstSelectSpin4 = false;//처음 한번 들어오면 false로 바꿔줘서 다음에 들어올때 위에 내용을 실행할 수 있음
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spin_Reserve5_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!checkFirstSelectSpin5) {
                    String weekFullName = "";
                    String weekShotName = "";
                    weekFullName = spin_Reserve5_week.getItemAtPosition(position).toString();//스피너에서 고른것을 스피너위의 테스트뷰에 표시해주기 위해
                    weekShotName = weekFullName.substring(0, 3);
                    txt_Reserve5_week.setText(weekShotName);
                }
                checkFirstSelectSpin5 = false;//처음 한번 들어오면 false로 바꿔줘서 다음에 들어올때 위에 내용을 실행할 수 있음
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_Reserve_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(ReservationActivity.this,
                        "Save Data...", null, true, true);
                String reserveData[] = new String[5];
                String checkFlag = "0";
                String weekPositionIndex;

                switch(txt_Reserve1_week.getText().toString()){
                    case "Sun":
                        weekPositionIndex = "0";
                        break;
                    case "Mon":
                        weekPositionIndex = "1";
                        break;
                    case "Tue":
                        weekPositionIndex = "2";
                        break;
                    case "Wed":
                        weekPositionIndex = "3";
                        break;
                    case "Thu":
                        weekPositionIndex = "4";
                        break;
                    case "Fri":
                        weekPositionIndex = "5";
                        break;
                    case "Sat":
                        weekPositionIndex = "6";
                        break;
                    case "Eve":
                        weekPositionIndex = "7";
                        break;
                    default:
                        weekPositionIndex =  "8";
                        break;
                }
                if(check_Reserve1.isChecked()){
                    checkFlag = "1";
                }
                else{
                    checkFlag = "0";
                }
                reserveData[0] = btn_Reserve1_Start.getText().toString()+"~"+btn_Reserve1_End.getText().toString()
                        +"-"+weekPositionIndex+"-"+checkFlag;
                Log.e("SaveTest",reserveData[0]);

                switch(txt_Reserve2_week.getText().toString()){
                    case "Sun":
                        weekPositionIndex = "0";
                        break;
                    case "Mon":
                        weekPositionIndex = "1";
                        break;
                    case "Tue":
                        weekPositionIndex = "2";
                        break;
                    case "Wed":
                        weekPositionIndex = "3";
                        break;
                    case "Thu":
                        weekPositionIndex = "4";
                        break;
                    case "Fri":
                        weekPositionIndex = "5";
                        break;
                    case "Sat":
                        weekPositionIndex = "6";
                        break;
                    case "Eve":
                        weekPositionIndex = "7";
                        break;
                    default:
                        weekPositionIndex =  "8";
                        break;
                }
                if(check_Reserve2.isChecked()){
                    checkFlag = "1";
                }
                else{
                    checkFlag = "0";
                }
                reserveData[1] = btn_Reserve2_Start.getText().toString()+"~"+btn_Reserve2_End.getText().toString()
                        +"-"+weekPositionIndex+"-"+checkFlag;
                Log.e("SaveTest",reserveData[1]);

                switch(txt_Reserve3_week.getText().toString()){
                    case "Sun":
                        weekPositionIndex = "0";
                        break;
                    case "Mon":
                        weekPositionIndex = "1";
                        break;
                    case "Tue":
                        weekPositionIndex = "2";
                        break;
                    case "Wed":
                        weekPositionIndex = "3";
                        break;
                    case "Thu":
                        weekPositionIndex = "4";
                        break;
                    case "Fri":
                        weekPositionIndex = "5";
                        break;
                    case "Sat":
                        weekPositionIndex = "6";
                        break;
                    case "Eve":
                        weekPositionIndex = "7";
                        break;
                    default:
                        weekPositionIndex =  "8";
                        break;
                }
                if(check_Reserve3.isChecked()){
                    checkFlag = "1";
                }
                else{
                    checkFlag = "0";
                }
                reserveData[2] = btn_Reserve3_Start.getText().toString()+"~"+btn_Reserve3_End.getText().toString()
                        +"-"+weekPositionIndex+"-"+checkFlag;
                Log.e("SaveTest",reserveData[2]);

                switch(txt_Reserve4_week.getText().toString()){
                    case "Sun":
                        weekPositionIndex = "0";
                        break;
                    case "Mon":
                        weekPositionIndex = "1";
                        break;
                    case "Tue":
                        weekPositionIndex = "2";
                        break;
                    case "Wed":
                        weekPositionIndex = "3";
                        break;
                    case "Thu":
                        weekPositionIndex = "4";
                        break;
                    case "Fri":
                        weekPositionIndex = "5";
                        break;
                    case "Sat":
                        weekPositionIndex = "6";
                        break;
                    case "Eve":
                        weekPositionIndex = "7";
                        break;
                    default:
                        weekPositionIndex =  "8";
                        break;
                }
                if(check_Reserve4.isChecked()){
                    checkFlag = "1";
                }
                else{
                    checkFlag = "0";
                }
                reserveData[3] = btn_Reserve4_Start.getText().toString()+"~"+btn_Reserve4_End.getText().toString()
                        +"-"+weekPositionIndex+"-"+checkFlag;
                Log.e("SaveTest",reserveData[3]);

                switch(txt_Reserve5_week.getText().toString()){
                    case "Sun":
                        weekPositionIndex = "0";
                        break;
                    case "Mon":
                        weekPositionIndex = "1";
                        break;
                    case "Tue":
                        weekPositionIndex = "2";
                        break;
                    case "Wed":
                        weekPositionIndex = "3";
                        break;
                    case "Thu":
                        weekPositionIndex = "4";
                        break;
                    case "Fri":
                        weekPositionIndex = "5";
                        break;
                    case "Sat":
                        weekPositionIndex = "6";
                        break;
                    case "Eve":
                        weekPositionIndex = "7";
                        break;
                    default:
                        weekPositionIndex =  "8";
                        break;
                }
                if(check_Reserve5.isChecked()){
                    checkFlag = "1";
                }
                else{
                    checkFlag = "0";
                }
                reserveData[4] = btn_Reserve5_Start.getText().toString()+"~"+btn_Reserve5_End.getText().toString()
                        +"-"+weekPositionIndex+"-"+checkFlag;

                Log.e("SaveTest",reserveData[4]);
                SaveReservation saveReservation = new SaveReservation();
                saveReservation.execute(reserveData[0],reserveData[1],reserveData[2],reserveData[3],reserveData[4]);
            }
        });
    }

    private class SaveReservation extends AsyncTask<String, Void, String> {
        String errorString = null;
        String resData = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }


        @Override
        protected String doInBackground(String... params) {
            String reserve1 = params[0];
            String reserve2 = params[1];
            String reserve3 = params[2];
            String reserve4 = params[3];
            String reserve5 = params[4];
            //String serverURL = "http://115.93.115.205:8080/test/test_get_state.php";

            String getParameters = "user_id="+ MainActivity.userID+"&device_id="+device_id+"&reserve1="+reserve1+"&reserve2="+reserve2
                    +"&reserve3="+reserve3+"&reserve4="+reserve4+"&reserve5="+reserve5;
            String serverURL = Address.serverIP+ Address.addrEditReserve+getParameters;

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .build();
                Request request = new Request.Builder()
                        .url(serverURL)
                        .post(body)
                        .build();
                client.newCall(request).enqueue(callback);
                client.newCall(request).execute();
                return "";
            } catch (Exception e) {
                Log.e("data", "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
        private Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                resData = response.body().string();
                Log.e("resData",resData);
                handler.sendEmptyMessage(PROGRESSDIALOG);
                //showList();
            }
        };
    }

    //TimePicker생성
    @SuppressLint("ValidFragment")
    public static class TimePickerFragment extends android.support.v4.app.DialogFragment
            implements TimePickerDialog.OnTimeSetListener{
        Button button;
        public TimePickerFragment(Button button) {
            this.button = button;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hour = String.format("%02d",hourOfDay);
            String min = String.format("%02d",minute);
            button.setText(hour+":"+min);
            Log.e("TimePickerFragment","Test1");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            c.get(Calendar.HOUR_OF_DAY);
            TimePickerDialog mTimePickerDialog = new TimePickerDialog(
                    getActivity(),this,24,00, true);
            return mTimePickerDialog;
        }
    }

    public final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //main activity에서 주기적으로 서버의 공기청정기 데이터를 파싱하면 알려줘서 device_fragment에서 UI 작업을 한다.
                case PROGRESSDIALOG:
                    Toast.makeText(ReservationActivity.this, "Save Succed.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                    break;
            }
        }
    };
}



