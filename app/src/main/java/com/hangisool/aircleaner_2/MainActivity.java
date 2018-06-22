package com.hangisool.aircleaner_2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hangisool.aircleaner_2.DeviceConnect.CheckDeviceActivity;
import com.hangisool.aircleaner_2.Util.Address;
import com.hangisool.aircleaner_2.DeviceConnect.WifiSelectActivity;
import com.hangisool.aircleaner_2.login.LoginActivity;
import com.hangisool.aircleaner_2.setting.ReservationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int UPDATE_STATE = 0;
    public static final int PROGRESSDIALOG = 1;
    public static final int ADDMENULIST = 3;
    DeviceStateFragment fragmentDeviceState;
    private static String TAG = "phpquerytest";
    public static String choiceDeviceName;
    public static String myJSON = null;
    String mJsonString;
    Menu menu;
    static boolean firstFlag;//처음에 한번만 해야하는 데이터 파싱후 목록 구성같은것을 하기위해 필요
    NavigationView navigationView;
    public static JSONArray jarray;

    //네비게이션 드로어 목록 UI그리는 작업을 위한 핸들러
    Handler mHandler = null;
    //사용자가 선택한 공기청정기의 device_id
    static String common_device_id = null;
    //사용자가 선택한 공기청정기의 json 배열의 순서넘버
    static int common_device_number;

    ProgressDialog progressDialog;

    public static String userID = "";

    TextView nav_heaer_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);
        Log.e("Activity LIfeCycle","onCreate()");
        setContentView(R.layout.activity_main);

        //마쉬멜로우 이상에서 특정권한 필요할때 체크하는 라이브러리 사용 TedPermission Library
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Log.e("Permission","Permission Granted");
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                Log.e("Permission","Permission Denied\n" + deniedPermissions.toString());
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
        //TedPermission Library

        Intent intent = getIntent();
        userID = intent.getExtras().getString("user_id");//이전 액티비티인 로그인 액티비티에서 회원 아이디를 가져온다.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#acb6e5")));
        firstFlag = true;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nav_heaer_username = navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        nav_heaer_username.setText(userID);

        //mainactivity에서 기기들의 데이터를 파싱한다.
        DeviceStateThread deviceStateThread = new DeviceStateThread();
        deviceStateThread.start();

        mHandler = new Handler();

        progressDialog = ProgressDialog.show(MainActivity.this,
                "Loading...", null, true, true);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("Activity LIfeCycle","onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Activity LIfeCycle","onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Activity LIfeCycle","onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Activity LIfeCycle","onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Activity LIfeCycle","onDestroy()");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            SharedPreferences auto = getSharedPreferences("auto_login", Activity.MODE_PRIVATE);
            //logout을 위해 저장되어있던 id와 pw를 null로 변경한다.
            SharedPreferences.Editor autoLogin = auto.edit();
            autoLogin.putString("inputId", null);
            autoLogin.putString("inputPwd", null);
            autoLogin.commit();
            Toast.makeText(MainActivity.this, userID +"Logout Succed.", Toast.LENGTH_SHORT).show();
            //로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if (id == R.id.deleteDevice) {//delete device 버튼 터치시
            //현재 프라그먼트 디바이스를 지운다.
            SharedPreferences auto = getSharedPreferences("auto_login", Activity.MODE_PRIVATE);
            //DB에서 해당 디바이스를 삭제한다.
            DeleteDevice deleteDevice = new DeleteDevice();
            deleteDevice.execute();
            Toast.makeText(MainActivity.this, userID +" Device Delete Succed.", Toast.LENGTH_SHORT).show();
            //로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if (id == R.id.btn_reservation) {//Reservation 버튼 터치시
            //예약 액티비티 활성화
            if(fragmentDeviceState != null) {
                Intent intent = new Intent(getApplicationContext(), ReservationActivity.class);
                intent.putExtra("data",
                        fragmentDeviceState.reservation1 + "/" + fragmentDeviceState.reservation2 + "/" + fragmentDeviceState.reservation3 + "/"
                                + fragmentDeviceState.reservation4 + "/" + fragmentDeviceState.reservation5);
                intent.putExtra("device_id",
                        fragmentDeviceState.device_id);
                startActivityForResult(intent, 1);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(!myJSON.equals("null")) {//서버에서 가져온 사용자의 디바이스 리스트가 있을때
            if(id == jarray.length()+1){//그중 마지막 리스트아이템인 add device를 터치 했을 경우
                Log.e("touch Addlist","when device list not null");

                //기기를 추가하는 로직을 시작한다.
                //CheckDeviceActivity 이동 ( 순서는 CheckDeviceActivity -> WifiSelectActivity -> APConnectActivity )
                Intent intent = new Intent(MainActivity.this, CheckDeviceActivity.class);
                startActivity(intent);
                //finish();
            }else {
                try {
                    JSONObject jobject = jarray.getJSONObject(id);
                    common_device_number = id;
                    common_device_id = jobject.getString("device_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setTitle(common_device_id);
                startFragmentDeviceState();
            }

        }else{//사용자에게 등록된 디바이스가 존재하지 않을때(서버에서 받아온 디바이스정보 리스트가 null일때)
            Log.e("touch Addlist","when device list null");
            //기기를 추가하는 로직을 시작한다.
            //CheckDeviceActivity 이동
            Intent intent = new Intent(MainActivity.this, CheckDeviceActivity.class);
            startActivity(intent);
            //finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //main activity에서 주기적으로 서버의 공기청정기 데이터를 파싱하면 알려줘서 device_fragment에서 UI 작업을 한다.
                case UPDATE_STATE:
                    startFragmentDeviceState();
                    break;
                case PROGRESSDIALOG:
                    progressDialog.dismiss();
                    break;
                case ADDMENULIST://사용자가 가지고 있는 디바이스가 없을경우 디바이스 추가버튼을 만들어주는 로직
                    menu.add(R.id.my_device_group, 0, 0, "Add device to touch");//기기 추가 리스트 버튼 추가
                    break;
            }
        }
    };
    public void startFragmentDeviceState(){
        //fragment에 bundle을 이용하여 데이터 전달
        fragmentDeviceState = DeviceStateFragment.newInstance();
        Bundle bundle = new Bundle(1);
        bundle.putString("JSONDATA",myJSON);
        fragmentDeviceState.setArguments(bundle);
        //DeviceStateFragment 실행
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_activity_main,fragmentDeviceState).commit();
    }

    private class DeleteDevice extends AsyncTask<String, Void, String> {
        String errorString = null;

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
            String getParameters = "user_id="+MainActivity.userID+"&device_id="+common_device_id;
            String serverURL = Address.serverIP+Address.addrDeleteDevice+getParameters;
            Log.e("deletdevice",getParameters);

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
                //showList();
            }
        };
    }

    class DeviceStateThread extends Thread {

        private static final String TAG = "deviceStateThread";
        public DeviceStateThread() {
            // 초기화 작업
        }

        @Override
        public void run() {
            super.run();
            // 스레드에게 수행시킬 동작들 구현
            String getParameters = "user_id="+userID;
            String serverURL = Address.serverIP+Address.addrGetState+ getParameters;
            myJSON = null;
            while (true && !MainActivity.this.isFinishing()) {
                try {
                    //3초에 한번 데이터 파싱
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .build();
                    Request request = new Request.Builder()
                            .url(serverURL)
                            .post(body)
                            .build();
                    client.newCall(request).enqueue(callback);
                    client.newCall(request).execute();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                menu = navigationView.getMenu();//네비게이션 드로어 메뉴 객체 가져오기
                myJSON = response.body().string();
                try {
                    jarray = new JSONArray(myJSON);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.e("myJson",myJSON);
                //디바이스 목록 드로어에 그려주는 UI작업(액티비티 첫 시작시 한번만)
                //myJSON이 null이면 이 유저는 장치를 등록 하지 않은것임
                if(firstFlag) {//처음으로 쓰레드 실행될때
                    if(!(myJSON.equals("null"))) {// 해당 아이디에 등록된 기기가 있을경우 **
                        Log.e("myJSON2", myJSON);
                        Log.e("firstFlag", "true");
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //디바이스 목록 드로어에 그려주는 UI작업
                                        try {
                                            jarray = new JSONArray(myJSON);
                                            // 네비게이션 드로어 목록에 디바이스 갯수만큼 그 디바이스 이름이 쓰인 아이템들을 추가한다.
                                            for (int i = 0; i < jarray.length(); i++) {
                                                JSONObject jobject = jarray.getJSONObject(i);
                                                String device_id = jobject.getString("device_id");
                                                menu.add(R.id.my_device_group, i, 0, device_id);
                                                if (i == 0) {
                                                    setTitle(device_id);//액션바 텍스트 변경 첫번째 기기 아이디로
                                                    common_device_id = device_id;//처음시작할때 common_device_id에 첫번째 기기이름을 삽입 ( 기기 선택을 안한 상태 (처음에 첫번째 기기가 선택되어있을때) 에서 deletedevice로 첫번째 기기를 지우기 위함)
                                                    common_device_number = 0;//프래그먼트 데이터 첫번째 기기 아이디의 정보 삽입
                                                }
                                            }
                                            menu.add(R.id.my_device_group, jarray.length() + 1, 0, "Add device to touch");//기기 추가 리스트 버튼 추가
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                        });
                        handler.sendEmptyMessage(PROGRESSDIALOG);//프로그래스다이얼로그 제거
                        handler.sendEmptyMessage(UPDATE_STATE);//프라그먼트 commit하는 핸들러 메시지
                        t.start();
                    }
                    else if(myJSON.equals("null")){//해당 아이디에 등록된 기기가 없을경우**
                        //프로그래스다이얼로그 제거
                        handler.sendEmptyMessage(PROGRESSDIALOG);
                        //디바이스 추가 버튼 만들어주기
                        handler.sendEmptyMessage(ADDMENULIST);
                        //CheckDeviceActivity 이동
                        Intent intent = new Intent(MainActivity.this, CheckDeviceActivity.class);
                        startActivity(intent);
                    }
                    firstFlag = false;//다음 쓰레드 실행시기에 이 로직 안을 안들어오게
                }
                if(fragmentDeviceState != null) {
                    fragmentDeviceState.handler.sendEmptyMessage(UPDATE_STATE);//commit된 프라그먼트의 데이터를 업데이트 해주는 핸들러 메시지
                }
            }
        };
    }
}
