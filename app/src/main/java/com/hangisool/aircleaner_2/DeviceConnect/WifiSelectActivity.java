package com.hangisool.aircleaner_2.DeviceConnect;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hangisool.aircleaner_2.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by L G on 2018-03-21.
 */
//AP가 연결 해야할 와이파이를 선택하고 패스워드가 맞는지 확인하는 액티비티
public class WifiSelectActivity extends AppCompatActivity {
    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler mHandler = new Handler();
    private BroadcastReceiver mWifiScanReceiver = null;

    private ConnectivityManager cManager;
    private NetworkInfo wifi;
    private ServerSocket serverSocket;

    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    private int scanCount = 0;
    private List<ScanResult> scanResult;
    private Spinner spinner;
    ArrayList<String>list;
    ArrayAdapter spinnerAdapter;
    public static final int WIFILISTUPDATE = 1;
    public String selectedWIFI;
    boolean scanFlag = true;
    Button btnWIFIResearch;
    Button btnWIFICheck;
    EditText edtWIFIPW;
    String wifiID;
    String wifiPassword;

    //와이파이 목록을 가져올때까지 돌아가는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_select);
        spinner = (Spinner)findViewById(R.id.wifi_list);
        btnWIFIResearch = (Button)findViewById(R.id.btn_wifi_research);
        btnWIFICheck = (Button)findViewById(R.id.btn_wifi_check);
        edtWIFIPW = (EditText)findViewById(R.id.wifi_pw);

        cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        registerReceiver();
        initWIFIScan();

        //event listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWIFI = spinner.getItemAtPosition(position).toString();
                //Toast.makeText(getApplicationContext(),"선택된 아이템 : "+spinner.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnWIFIResearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFlag = true;
                progressDialog = ProgressDialog.show(WifiSelectActivity.this,
                        "WIFI List Loading...", null, true, true);
            }
        });
        btnWIFICheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //사용자가 입력한 와이파이 아이디와 비밀번호를 가지고 와이파이에 연결해보아 와이파이비밀번호가 정확한지 알아보는 로직
                wifiID = selectedWIFI;
                wifiPassword = edtWIFIPW.getText().toString();
                //Toast.makeText(WifiSelectActivity.this, wifiID, Toast.LENGTH_SHORT).show();
                //Toast.makeText(WifiSelectActivity.this, wifiPassword, Toast.LENGTH_SHORT).show();

                ConnectivityManager manager;
                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);//와이파이 연결 체크
                if(wifi.isConnected()){
                    Log.e("WIFI","와이파이 연결 되어있음");
                }
                else{
                    Log.e("WIFI","와이파이 연결 안되어있음");
                    //Toast.makeText(getApplicationContext(),"WIFI ON",Toast.LENGTH_SHORT).show();
                    wifiManager.setWifiEnabled(true);
                }

                if(checkAP(wifiID,wifiPassword)){//아이디와 패스워드가 맞으면 true를 반환한다.
                    Log.e("WIFI", "현재정보로 연결가능");
                    Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(WifiSelectActivity.this,APConnectActivity.class);
                    intent.putExtra("WIFI_ID",wifiID);
                    intent.putExtra("WIFI_PW",wifiPassword);
                    finish();
                    startActivity(intent);
                }else{
                    Log.e("WIFI", "비밀번호 잘못됨");
                    Toast.makeText(getApplicationContext(),"Please Check password",Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressDialog = ProgressDialog.show(WifiSelectActivity.this,
                "WIFI List Loading...", null, true, true);
    }

    public boolean checkAP(String ssid, String passkey) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String networkSSID = ssid;
        String networkPass = passkey;

        List<ScanResult> scanResultList = wifiManager.getScanResults();

        for (ScanResult result : scanResultList) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);

                if (securityMode.equalsIgnoreCase("OPEN")) {
                    Log.e("securityMode","OPEN");
                    wifiPassword = "empty";//비밀번호가 없는 와이파이 일 때 공기청정기에게 넘겨주는 비밀번호를 "empty"로 한다.
                   return true;
                } else if (securityMode.equalsIgnoreCase("WEP")) {
                    Log.e("securityMode","WEP");
                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    boolean b = wifiManager.enableNetwork(res, true);
                    if(b == true){//와이파이 패스워드와 아이디가 맞을경우 true를 반환
                        return true;
                    }else{
                        return false;
                    }
                }
                Log.e("securityMode","other");
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";

                wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
                /*wifiConfiguration.hiddenSSID = true;
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);*/

                int res = wifiManager.addNetwork(wifiConfiguration);
                boolean b = wifiManager.enableNetwork(res, true);
                if(b == true){//와이파이 패스워드와 아이디가 맞을경우 true를 반환
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    void connect(String ID, String PW){
        WifiConfiguration wificonfig=new WifiConfiguration();
        wificonfig.SSID = String.format("\"%s\"", ID);
        if(PW.equals("")){//비밀번호가 없을경우

        }else{//비밀번호가 있을경우

        }
        wificonfig.preSharedKey = String.format("\"%s\"", PW);

        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wificonfig);
        //wifiManager.disconnect();
        //만약 연결가능한 와이파이 라면 연결
        if(wifiManager.enableNetwork(netId, true)) {
            Log.e("WIFI", "현재정보로 연결가능");
            Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(WifiSelectActivity.this,APConnectActivity.class);
            intent.putExtra("WIFI_ID",ID);
            intent.putExtra("WIFI_PW",PW);
            finish();
            startActivity(intent);
        }else{
            Log.e("WIFI", "비밀번호 잘못됨");
            Toast.makeText(getApplicationContext(),"Please Check password",Toast.LENGTH_SHORT).show();
        }
    }

    Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case WIFILISTUPDATE:
                    spinnerAdapter = new ArrayAdapter(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, list);
                    spinner.setDrawingCacheBackgroundColor(Color.BLACK);
                    spinner.setAdapter(spinnerAdapter);
                    break;
            }
        }
    };
    private void registerReceiver() {
        /** 1. intent filter를 만든다
         *  2. intent filter에 action을 추가한다.
         *  3. BroadCastReceiver를 익명클래스로 구현한다.
         *  4. intent filter와 BroadCastReceiver를 등록한다.
         * */
        if (mWifiScanReceiver != null) return;

        final IntentFilter theFilter = new IntentFilter();

        this.mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    if(scanFlag) {
                        scanFlag = false;
                        getWIFIScanResult();
                        progressDialog.dismiss();
                    }
                    wifiManager.startScan();
                } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    //getApplicationContext().sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
                }
            }
        };
        this.registerReceiver(this.mWifiScanReceiver, theFilter);
    }
    public void initWIFIScan(){
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        scanCount = 0;
        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(mWifiScanReceiver, filter);
        wifiManager.startScan();
    }

    public void getWIFIScanResult(){
        scanResult = wifiManager.getScanResults();
        //tv4.setText("");
        list = new ArrayList<>();
        for (int i = 0; i < scanResult.size(); i++) {
            ScanResult result = scanResult.get(i);
            Log.e("WIFILIST",(i + 1) + ". SSID : " + result.SSID.toString());
            list.add(result.SSID.toString());
        }
        String firstWIFIName = scanResult.get(0).SSID.toString();
        selectedWIFI = firstWIFIName;
        uiHandler.sendEmptyMessage(WIFILISTUPDATE);
    }
    /** 동적으로(코드상으로) 브로드 캐스트를 종료한다. **/
    private void unregisterReceiver() {
        if(mWifiScanReceiver != null){
            this.unregisterReceiver(mWifiScanReceiver);
            mWifiScanReceiver = null;
        }

    }
    public String getScanResultSecurity(ScanResult scanResult) {
        //Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }
}
