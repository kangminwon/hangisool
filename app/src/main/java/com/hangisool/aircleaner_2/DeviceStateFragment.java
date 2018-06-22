package com.hangisool.aircleaner_2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hangisool.aircleaner_2.Util.Address;
import com.hangisool.aircleaner_2.setting.ReservationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hangisool.aircleaner_2.MainActivity.UPDATE_STATE;

/**
 * Created by L G on 2018-02-23.
 */

public class DeviceStateFragment extends android.support.v4.app.Fragment {

    View view;
    private static String TAG = "phpquerytest";
    static final int BUTTON_STATE = 2;
    static final int SPEED_STATE = 3;
    static final int LIGHT_STATE = 4;

    private TextView mTextViewResult;
    ArrayList<HashMap<String, String>> mArrayList;
    ListView mListViewList;
    EditText mEditTextSearchKeyword;
    String mJsonString;

    //공기청정기 상태 String
    String device_id = "";
    String temperature = "";
    String humidity = "";
    String dust_large = "";
    String dust_small = "";
    String pressure = "";
    String gas = "";
    String speed = "";
    String speed_control = "";
    String device_mode = "";
    String device_mode_control = "";
    String reservation1 = "";
    String reservation2 = "";
    String reservation3 = "";
    String reservation4 = "";
    String reservation5 = "";
    String network = "";
    String fan_open = "";
    String air_state = "";
    String fan_life = "";//필터교환시기 계
    boolean fan_open_check = false;
    String tmp_network = "";//네트웍패킷이 바뀌는지 확인하기위해 임시로 저장하는 공간
    int cnt_network = 0;

    //표시 컴포넌트
    TextView txtTemperature;
    TextView txtHumidity;
    TextView txtAirCondition;
    TextView txtSmallDust;
    TextView txtFilterLife;
    TextView txtReservation;
    TextView txtNetwork;

    //버튼 컴포넌트
    Button btn_Power;
    Button btn_manual;
    Button btn_Sleep;
    Button btn_Auto;
    RelativeLayout btn_reservation;
    RelativeLayout btn_air_condition;
    RelativeLayout btn_filter_life;
    RelativeLayout btn_humidity;
    RelativeLayout btn_small_dust;
    RelativeLayout btn_temperature;
    TextView btn_speed_1;
    TextView btn_speed_2;
    TextView btn_speed_3;

    String tmp_Mode = "";
    String tmp_Speed = "";
    String tmp_Light = "";

    AlertDialog.Builder alertBuilder;//네트워크 불안정시 알림창 표출

    public static DeviceStateFragment newInstance() {
        DeviceStateFragment fragment = new DeviceStateFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_state, container, false);
        String JsonData = getArguments().getString("JSONDATA");
        //Log.e("JSONDATA",JsonData);

        btn_Power = (Button)view.findViewById(R.id.btn_power);
        btn_manual = (Button)view.findViewById(R.id.btn_manual);
        btn_Sleep = (Button)view.findViewById(R.id.btn_sleep);
        btn_Auto = (Button)view.findViewById(R.id.btn_auto);
        btn_reservation = (RelativeLayout)view.findViewById(R.id.btn_reservation);
        btn_air_condition = (RelativeLayout)view.findViewById(R.id.btn_air_condition);
        btn_filter_life = (RelativeLayout)view.findViewById(R.id.btn_filter_life);
        btn_humidity = (RelativeLayout)view.findViewById(R.id.btn_humidity);
        btn_small_dust = (RelativeLayout)view.findViewById(R.id.btn_small_dust);
        btn_temperature = (RelativeLayout)view.findViewById(R.id.btn_temperature);

        txtTemperature = (TextView)view.findViewById(R.id.txt_temperature);//온도
        txtHumidity = (TextView)view.findViewById(R.id.txt_humidity);//습도
        txtAirCondition = (TextView)view.findViewById(R.id.txt_air_condition);//공기상태
        txtSmallDust = (TextView)view.findViewById(R.id.txt_small_dust);//미세먼지량(작은거)
        txtFilterLife = (TextView)view.findViewById(R.id.txt_filterlife);//필터교체시간
        txtReservation = (TextView)view.findViewById(R.id.txt_reservation);//예약상태
        txtNetwork = (TextView)view.findViewById(R.id.txt_network);
        btn_speed_1 = (TextView)view.findViewById(R.id.btn_speed1);
        btn_speed_2 = (TextView)view.findViewById(R.id.btn_speed2);
        btn_speed_3 = (TextView)view.findViewById(R.id.btn_speed3);

        alertBuilder = new AlertDialog.Builder(view.getContext());

        //정보 테이블 레이아웃 버튼 모음
        btn_reservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), ReservationActivity.class);
                intent.putExtra("data",
                        reservation1+"/"+reservation2+"/"+reservation3+"/"+reservation4+"/"+reservation5);
                intent.putExtra("device_id",
                        device_id);
                startActivityForResult(intent,1);
            }
        });
        btn_air_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Toast.makeText(view.getContext(), "air_condition", Toast.LENGTH_SHORT).show();

            }
        });
        btn_speed_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlSpeed controlSpeed = new ControlSpeed();
                Log.e("speed_control",speed_control);
                speed_control = "1";
                controlSpeed.execute(speed_control);

                ControlMode controlMode = new ControlMode();
                String mode = "3";
                controlMode.execute(mode);
            }
        });
        btn_speed_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlSpeed controlSpeed = new ControlSpeed();
                Log.e("speed_control",speed_control);
                speed_control = "2";
                controlSpeed.execute(speed_control);

                ControlMode controlMode = new ControlMode();
                String mode = "3";
                controlMode.execute(mode);
            }
        });
        btn_speed_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlSpeed controlSpeed = new ControlSpeed();
                Log.e("speed_control",speed_control);
                speed_control = "3";
                controlSpeed.execute(speed_control);

                ControlMode controlMode = new ControlMode();
                String mode = "3";
                controlMode.execute(mode);
            }
        });
        btn_filter_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetFilterLIfe();
            }
        });
        btn_humidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(view.getContext(), "humidity", Toast.LENGTH_SHORT).show();
            }
        });
        btn_small_dust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(view.getContext(), "smallDust", Toast.LENGTH_SHORT).show();
            }
        });
        btn_temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(view.getContext(), "Temperature", Toast.LENGTH_SHORT).show();
            }
        });

        btn_Auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlMode controlMode = new ControlMode();
                String mode;
                if(device_mode_control.equals("1")) {//sleep모드에서 다시 sleep모드를 누르면
                    mode = "3";//수동모드로 변환
                }else{//sleep모드가 아닐시
                    mode = "1";//sleep모드로 변환
                }
                controlMode.execute(mode);
                //Toast.makeText(view.getContext(), "Auto", Toast.LENGTH_SHORT).show();
            }
        });
        btn_Sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mode;
                if(device_mode_control.equals("2")) {//sleep모드에서 다시 sleep모드를 누르면
                    mode = "3";//수동모드로 변환
                    ControlMode controlMode = new ControlMode();
                    controlMode.execute(mode);
                }else{//sleep모드가 아닐시
                    showSleepTimePicker();
                }
            }
        });

        btn_Power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlMode controlMode = new ControlMode();
                String mode;
                if(device_mode_control.equals("0")){//전원이 꺼져있을때 전원 버튼을 터치하면
                    mode = "3";//수동모드로 전환
                }else{//전원이 켜져있을때 전원 버튼을 터치하면
                    mode = "0";//전원off모드로 전환
                }
                controlMode.execute(mode);
                //Toast.makeText(view.getContext(), "Power", Toast.LENGTH_SHORT).show();
            }
        });

        settingStateTable(MainActivity.myJSON);
        return view;
    }

    public final Handler handler = new Handler(){
        @SuppressLint("ResourceType")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //main activity에서 주기적으로 서버의 공기청정기 데이터를 파싱 device_fragment도 주기적으로 UI 작업을 한다.
                case UPDATE_STATE:
                    settingStateTable(MainActivity.myJSON);
                    break;
                case BUTTON_STATE:
                    btn_Power.setBackgroundResource(R.drawable.ic_power_settings_new_black_24dp);
                    btn_Auto.setBackgroundResource(R.drawable.ic_autorenew_black_24dp);
                    btn_Sleep.setBackgroundResource(R.drawable.ic_brightness_2_black_24dp);
                    btn_manual.setBackgroundResource(R.drawable.baseline_favorite_border_black_48dp);
                    if(!device_mode_control.equals("0")) {//device power off
                        btn_Power.setBackgroundResource(R.drawable.ic_power_settings_new_black_24dp_on);
                        if(device_mode_control.equals("1")) {
                            btn_Auto.setBackgroundResource(R.drawable.ic_autorenew_black_24dp_on);
                        }else if(device_mode_control.equals("2")){
                            btn_Sleep.setBackgroundResource(R.drawable.ic_brightness_2_black_24dp_on);
                        }else if(device_mode_control.equals("2")){
                            btn_Sleep.setBackgroundResource(R.drawable.baseline_favorite_border_black_48dp_on);
                        }
                    }
                    break;
                case SPEED_STATE:
                    if(speed_control.equals("1")){
                        btn_speed_1.setBackgroundResource(R.drawable.speed_btn_color);
                        btn_speed_2.setBackgroundResource(Color.TRANSPARENT);
                        btn_speed_3.setBackgroundResource(Color.TRANSPARENT);
                    }else if(speed_control.equals("2")){
                        btn_speed_1.setBackgroundResource(Color.TRANSPARENT);
                        btn_speed_2.setBackgroundResource(R.drawable.speed_btn_color);
                        btn_speed_3.setBackgroundResource(Color.TRANSPARENT);
                    }else if(speed_control.equals("3")){
                        btn_speed_1.setBackgroundResource(Color.TRANSPARENT);
                        btn_speed_2.setBackgroundResource(Color.TRANSPARENT);
                        btn_speed_3.setBackgroundResource(R.drawable.speed_btn_color);
                    }
                    break;
            }
        }
    };

    @SuppressLint("ResourceType")
    void settingStateTable(String bundleArguments){
        try {
            JSONArray jsonArray = new JSONArray(bundleArguments);
            //for(int i=0; i<jsonArray.length(); i++){
                JSONObject jobject = jsonArray.getJSONObject(MainActivity.common_device_number);
                device_id = jobject.getString("device_id");
                temperature = jobject.getString("temperature");
                humidity = jobject.getString("humidity");
                dust_large = jobject.getString("dust_large");
                dust_small = jobject.getString("dust_small");
                pressure = jobject.getString("pressure");
                gas = jobject.getString("gas");
                speed = jobject.getString("speed");
                speed_control = jobject.getString("speed_control");
                device_mode = jobject.getString("device_mode");
                device_mode_control = jobject.getString("device_mode_control");
                reservation1 = jobject.getString("reservation1");
                reservation2 = jobject.getString("reservation2");
                reservation3 = jobject.getString("reservation3");
                reservation4 = jobject.getString("reservation4");
                reservation5 = jobject.getString("reservation5");
                network = jobject.getString("network");
                fan_open = jobject.getString("fan_open");
                air_state = jobject.getString("air_state");
                fan_life = jobject.getString("fan_life");

                btn_Power.setBackgroundResource(R.drawable.ic_power_settings_new_black_24dp);
                btn_Auto.setBackgroundResource(R.drawable.ic_autorenew_black_24dp);
                btn_Sleep.setBackgroundResource(R.drawable.ic_brightness_2_black_24dp);
                btn_manual.setBackgroundResource(R.drawable.baseline_favorite_border_black_48dp);
                //device mode setting
                if(!device_mode_control.equals("0")){//device power on
                    btn_Power.setBackgroundResource(R.drawable.ic_power_settings_new_black_24dp_on);
                    if(device_mode_control.equals("1")){
                        btn_Auto.setBackgroundResource(R.drawable.ic_autorenew_black_24dp_on);
                    }
                    else if(device_mode_control.equals("2")){
                        btn_Sleep.setBackgroundResource(R.drawable.ic_brightness_2_black_24dp_on);
                    }
                    else if(device_mode_control.equals("2")){
                        btn_Sleep.setBackgroundResource(R.drawable.baseline_favorite_border_black_48dp_on);
                    }
                }
                txtTemperature.setText(temperature);
                txtHumidity.setText(humidity);

                //Air Condition judgment
                String airCondition = "";
                if (air_state.equals("3")) {//very bad
                    airCondition = "VERY BAD";
                    txtAirCondition.setText(airCondition);
                    //txtAirCondition.setTextColor(ContextCompat.getColor(getContext(), R.color.air_condition_verybad));
                } else if (air_state.equals("2")) {//bad
                    airCondition = "BAD";
                    txtAirCondition.setText(airCondition);
                    //txtAirCondition.setTextColor(ContextCompat.getColor(getContext(),R.color.air_condition_bad));
                } else if (air_state.equals("1")) {//Normal
                    airCondition = "NORMAL";
                    txtAirCondition.setText(airCondition);
                    //txtAirCondition.setTextColor(ContextCompat.getColor(getContext(),R.color.air_condition_normal));
                } else if (air_state.equals("0")){//good
                    airCondition = "GOOD";
                    txtAirCondition.setText(airCondition);
                    //txtAirCondition.setTextColor(ContextCompat.getColor(getContext(),R.color.air_condition_good));
                }


                if(speed_control.equals("1")){
                    btn_speed_1.setBackgroundResource(R.drawable.speed_btn_color);
                    btn_speed_2.setBackgroundResource(Color.TRANSPARENT);
                    btn_speed_3.setBackgroundResource(Color.TRANSPARENT);
                }else if(speed_control.equals("2")){
                    btn_speed_1.setBackgroundResource(Color.TRANSPARENT);
                    btn_speed_2.setBackgroundResource(R.drawable.speed_btn_color);
                    btn_speed_3.setBackgroundResource(Color.TRANSPARENT);
                }else if(speed_control.equals("3")){
                    btn_speed_1.setBackgroundResource(Color.TRANSPARENT);
                    btn_speed_2.setBackgroundResource(Color.TRANSPARENT);
                    btn_speed_3.setBackgroundResource(R.drawable.speed_btn_color);
                }

                if(network.equals(tmp_network)){//이전 네트웍 패킷과 새로운 네트웍 패킷이 같으면
                    cnt_network+=1;
                }else{//이전과 다른패킷이 들어와 공기청정기가 서버와 통신하고 있다는게 true이면
                    cnt_network = 0;
                }
                tmp_network = network;

                if(cnt_network == 4) {//4번이상 같은 패킷이 호출되면(공기청정기가 네트웍에 연결이 안되어있으면)
                    //네트웍 불안정시 알림창 표출
                    alertBuilder
                            .setTitle("Notice")
                            .setMessage("Please Check the WiFi connection of the air cleaner.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cnt_network = 0;
                                }
                            });
                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                }else {
                    if (fan_open.equals("1") && fan_open_check == false) {//공기청정기 뚜껑이 열려있으면 && 팬오픈 알림이 떳을때 확인버튼을 터치하기 전까지 다시 알림이 뜨지 않게 함
                        fan_open_check = true;
                        //뚜껑열림 알림창 표출
                        alertBuilder
                                .setTitle("Notice")
                                .setMessage("Cap is opened")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        fan_open_check = false;
                                    }
                                });
                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }
                }

                txtSmallDust.setText(dust_small);
                txtFilterLife.setText(fan_life);
                //txtBigDust.setText(dust_large);
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //DeviceStateThread deviceStateThread = new DeviceStateThread();
        //deviceStateThread.start();
    }

    private class ControlSpeed extends AsyncTask<String, Void, String> {
        String errorString = null;
        String myJSON_Fragment = null;

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
            tmp_Speed = params[0];

            String getParameters = "user_id="+MainActivity.userID+"&device_id="+device_id+"&speed_control="+tmp_Speed;
            String serverURL = Address.serverIP+Address.addrSpeedControl+getParameters;

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
                Log.e(TAG, "InsertData: Error ", e);
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
                myJSON_Fragment = response.body().string();
                speed_control = tmp_Speed;
                handler.sendEmptyMessage(SPEED_STATE);//속도변경이 성공하면 속도의 텍스트를 바꿔준다.
                Log.e("myJSON",myJSON_Fragment);
                //showList();
            }
        };
    }

    private class ControlMode extends AsyncTask<String, Void, String> {
        String errorString = null;
        String myJSON_Fragment = null;

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
            tmp_Mode = params[0];
            //String serverURL = "http://115.93.115.205:8080/test/test_get_state.php";

            String getParameters = "user_id="+MainActivity.userID+"&device_id="+device_id+"&mode_control="+tmp_Mode;
            String serverURL = Address.serverIP+Address.addrModeControl+getParameters;

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
                Log.e(TAG, "InsertData: Error ", e);
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
                myJSON_Fragment = response.body().string();
                device_mode_control = tmp_Mode;
                handler.sendEmptyMessage(BUTTON_STATE);//모드변경이 성공하면 버튼의 이미지를 바꿔준다.
                Log.e("myJSON",myJSON_Fragment);
                //showList();
            }
        };
    }

    private class ResetFilterLife extends AsyncTask<String, Void, String> {
        String errorString = null;
        String myJSON_Fragment = null;

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

            String getParameters = "user_id="+MainActivity.userID+"&device_id="+device_id+"&filter_life_reset="+"1";
            String serverURL = Address.serverIP+Address.addrResetFilterLife+getParameters;

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
                Log.e(TAG, "InsertData: Error ", e);
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
                myJSON_Fragment = response.body().string();
                Log.e("myJSON",myJSON_Fragment);
                //showList();
            }
        };
    }

    private class ControlSleepTime extends AsyncTask<String, Void, String> {
        String errorString = null;
        String myJSON_Fragment = null;

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
            String sleepTime;
            sleepTime = params[0];
            //String serverURL = "http://115.93.115.205:8080/test/test_get_state.php";

            String getParameters = "user_id="+MainActivity.userID+"&device_id="+device_id+"&sleep_time="+sleepTime;
            String serverURL = Address.serverIP+Address.addrUpdateSleepTime+getParameters;

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
                Log.e(TAG, "InsertData: Error ", e);
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
                myJSON_Fragment = response.body().string();
                Log.e("myJSON",myJSON_Fragment);
                //showList();
            }
        };
    }

    private void showSleepTimePicker() {
        final Dialog sleeptimeDialog = new Dialog(view.getContext());
        sleeptimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sleeptimeDialog.setContentView(R.layout.dialog_sleeptime);

        Button okBtn = (Button) sleeptimeDialog.findViewById(R.id.timer_btn_ok);
        Button cancelBtn = (Button) sleeptimeDialog.findViewById(R.id.timer_btn_cancel);
        RadioGroup group=(RadioGroup)sleeptimeDialog.findViewById(R.id.radioGroup);
        RadioButton one=(RadioButton)sleeptimeDialog.findViewById(R.id.rb_one);
        RadioButton two=(RadioButton)sleeptimeDialog.findViewById(R.id.rb_two);
        RadioButton three=(RadioButton)sleeptimeDialog.findViewById(R.id.rb_three);
        RadioButton four=(RadioButton)sleeptimeDialog.findViewById(R.id.rb_four);

        final NumberPicker nph = (NumberPicker) sleeptimeDialog.findViewById(R.id.hourPicker);
        nph.setMinValue(0);
        nph.setMaxValue(8);
        nph.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(nph, android.R.color.white );
        nph.setWrapSelectorWheel(false);
        nph.setValue(0);
        nph.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

            }
        });

        final NumberPicker npm = (NumberPicker) sleeptimeDialog.findViewById(R.id.minPicker);
        npm.setMinValue(0);
        npm.setMaxValue(60);
        npm.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(npm, android.R.color.white );
        npm.setWrapSelectorWheel(false);
        npm.setValue(0);
        npm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minute;
                String minute_string;
                minute = npm.getValue()+(nph.getValue()*60);
                minute_string = String.valueOf(minute);
                String mode;
                if(device_mode_control.equals("2")) {//sleep모드에서 다시 sleep모드를 누르면
                    mode = "3";//수동모드로 변환
                }else{//sleep모드가 아닐시
                    mode = "2";//sleep모드로 변환
                    if(minute != 0) {//continue나 0분이 아닐경우에만
                        Toast.makeText(view.getContext(), "Power off after " + minute_string + " minute", Toast.LENGTH_SHORT).show();
                    }
                }

                ControlSleepTime controlSleepTime = new ControlSleepTime();
                controlSleepTime.execute(minute_string);

                ControlMode controlMode = new ControlMode();
                controlMode.execute(mode);

                sleeptimeDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sleeptimeDialog.dismiss();
            }
        });

        //radio group setting
        four.setChecked(true);//초기값은 continue로
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_one:
                        npm.setValue(30);
                        nph.setValue(0);
                        break;
                    case R.id.rb_two:
                        npm.setValue(60);
                        nph.setValue(0);
                        break;
                    case R.id.rb_three:
                        npm.setValue(30);
                        nph.setValue(1);
                        break;
                    case R.id.rb_four:
                        npm.setValue(0);
                        nph.setValue(0);
                        break;
                }
            }
        });

        sleeptimeDialog.show();
    }

    private void showResetFilterLIfe() {
        final Dialog resetFilterDialog = new Dialog(view.getContext());
        resetFilterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resetFilterDialog.setContentView(R.layout.dialog_resetfilter);

        Button btnNo = (Button) resetFilterDialog.findViewById(R.id.btn_resetfilter_no);
        Button btnYes = (Button) resetFilterDialog.findViewById(R.id.btn_resetfilter_yes);

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilterDialog.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetFilterLife resetFilterLife = new ResetFilterLife();
                resetFilterLife.execute();

                resetFilterDialog.dismiss();
            }
        });

        resetFilterDialog.show();
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
